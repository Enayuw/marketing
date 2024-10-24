package com.br.marketing.service.Impl.qifu;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.qifu.QiFuCuWanJianBatQryUserRealDto;
import com.br.marketing.dto.qifu.QiFuCuWanJianBatQryUserRealParamsDto;
import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.SynInfoQueryAction;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.SynInfoQueryActionMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * QiFuCuWanJianBatQryUserRealService
 *
 * @Author lixiang
 * @Date 2024-10-19
 */
@Service
@Slf4j
public class QiFuCuWanJianBatQryUserRealService {

    private final static String TITLE = "【360促完件用户信息批量查询】";

    ThreadPoolExecutor taskActionPool = BrExecutors.getThreadPool(4, 4);
    ThreadPoolExecutor queryActionPool = BrExecutors.getThreadPool(4, 4);

    private Integer PARTITION_SIZE = 50;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private SynInfoQueryActionMapper synInfoQueryActionMapper;

    @Resource
    private QiFuCuWanJianBatQryUserRealTransService qiFuCuWanJianBatQryUserRealTransService;

    public Result<Map<String, Object>> action(Page2Condition<QiFuCuWanJianBatQryUserRealDto> condition) {
        return scanData(condition);
    }
    public Result<Map<String, Object>> scanData(Page2Condition<QiFuCuWanJianBatQryUserRealDto> condition) {
        Result result = new Result().failure();
        Map<String, Object> data= new HashMap<>();

        QiFuCuWanJianBatQryUserRealDto param = condition.getParam();
        String apiCode = param.getApiCode();
        String paramTaskId = param.getTaskId();
        String bizDate = param.getBizDate();
        List<Integer> statusList = param.getStatusList();
        String actionDate = LocalDate.now().toString();
        Integer pageSize = condition.getPageSize();

        log.warn(TITLE + "scanData start, apiCode: {}, bizDate: {}, paramTaskId:{}, ", apiCode, bizDate, paramTaskId);
        long start = System.currentTimeMillis();
        try{
            // 循环获取条件数据，每次pageSize条
            Map<String, String> marketingTimeInterval = calculateTimeInterval(bizDate);
            String createTimeStart = marketingTimeInterval.get("createTimeStart");
            String createTimeEnd = marketingTimeInterval.get("createTimeEnd");
            List<MarketingSyncInfo> marketingSyncInfoList = marketingSyncInfoMapper.querySynInfoWithActiontikv_(apiCode
                    , statusList, actionDate, createTimeStart, createTimeEnd, paramTaskId, pageSize);
            if (CollectionUtils.isEmpty(marketingSyncInfoList)) {
                log.warn(TITLE+"scanData, 未获取到数据");
                data.put("hasScanData", "0");
                return new Result().success().setDate(data);
            }
            log.warn(TITLE + "scanData 获取到数据, size: {}", marketingSyncInfoList.size());

            // 1个批次1个线程，因为批次要更新执行状态
            Integer threadPoolSize = Integer.parseInt(String.valueOf(marketingCommonConfig.getQiFuCuWanJianBatQryUserRealConfigParams().get("taskActionPoolSize")));
            taskActionPool.setCorePoolSize(threadPoolSize);
            taskActionPool.setMaximumPoolSize(threadPoolSize);

            List<CompletableFuture<Void>> futures = Lists.newArrayList();
            for(MarketingSyncInfo marketingSyncInfo : marketingSyncInfoList) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        actionSyncInfo(apiCode, paramTaskId, actionDate, marketingSyncInfo);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(), TITLE + "taskAction异常"));
                    }
                }, taskActionPool));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            log.warn(TITLE+ e.getMessage(), e);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(), TITLE+ e.getMessage()));
        }
        long end = System.currentTimeMillis();
        log.warn(TITLE + "scanData end, cost: {}, apiCode: {}, bizDate: {}, paramTaskId:{}, ", end-start, apiCode, bizDate, paramTaskId);
        return result;
    }

    private Result actionSyncInfo(String apiCode, String paramTaskId, String actionDate, MarketingSyncInfo marketingSyncInfo) {
        Result result = new Result().failure();
        try {
            Long dataId = marketingSyncInfo.getId();
            String taskId = marketingSyncInfo.getCusBatch();
            String requestBatch = marketingSyncInfo.getRequestBatch();
            log.warn(TITLE + "actionSyncInfo, dataId: {}, taskId: {}", dataId, taskId);

            // saveAction
            SynInfoQueryAction queryAction = saveAction(dataId, apiCode, actionDate);

            // marketingSyncUserList
            List<MarketingSyncUser> marketingSyncUserList = marketingSyncUserMapper.getSyncUserByCondition(apiCode
                    , requestBatch, paramTaskId);

            // actionDataList
            Result<Map<String, Object>> actionResult = actionDetailList(apiCode, taskId, marketingSyncUserList);

            // updateActionStatus
            if (actionResult != null && actionResult.isSuccess()) {
                updateActionStatus(queryAction.getId(), 2);
                log.warn(TITLE + "action更新成功, dataId:{}, taskId:{}", dataId, taskId);
            }
        } catch (Exception e) {
            log.warn(TITLE + e.getMessage(), e);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(), TITLE + e.getMessage()));
        }
        return result.success();
    }

    private Result<Map<String, Object>> actionDetailList(String apiCode, String taskId, List<MarketingSyncUser> detailList) {
        log.warn(TITLE + "actionDataList start, apiCode: {}, taskId: {}", apiCode, taskId);
        long start = System.currentTimeMillis();
        Result result = new Result().failure();

        Integer queryActionPoolSize = Integer.parseInt(String.valueOf(marketingCommonConfig.getQiFuCuWanJianBatQryUserRealConfigParams().get("queryActionPoolSize")));
        queryActionPool.setCorePoolSize(queryActionPoolSize);
        queryActionPool.setMaximumPoolSize(queryActionPoolSize);
        Integer partitionSize = Integer.parseInt(String.valueOf(marketingCommonConfig.getQiFuCuWanJianBatQryUserRealConfigParams().get("partitionSize")));
        PARTITION_SIZE = partitionSize;

        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        List<List<MarketingSyncUser>> dataPartitions = Lists.partition(detailList, PARTITION_SIZE);
        for (List<MarketingSyncUser> partition : dataPartitions) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    QiFuCuWanJianBatQryUserRealParamsDto paramsDto = new QiFuCuWanJianBatQryUserRealParamsDto();
                    paramsDto.setApiCode(apiCode);
                    paramsDto.setTaskId(taskId);
                    paramsDto.setPartition(partition);
                    qiFuCuWanJianBatQryUserRealTransService.actionPartition(paramsDto, 0);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(), TITLE + "queryAction异常"));
                }
            }, queryActionPool));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long end = System.currentTimeMillis();
        log.warn(TITLE + "actionDataList end, cost: {}, apiCode: {}, taskId: {}", end-start, apiCode, taskId);
        return result.success();
    }

    private Map<String, String> calculateTimeInterval(String bizDate) throws ParseException {
        Map<String, String> res = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate bizLocalDate = LocalDate.parse(bizDate, formatter);
        LocalDate endLocalDate = bizLocalDate.plusDays(1);

        res.put("createTimeStart", bizLocalDate.toString());
        res.put("createTimeEnd", endLocalDate.toString());
        return res;
    }

    public SynInfoQueryAction saveAction(Long dataId, String apiCode, String actionDate) {
        SynInfoQueryAction queryAction = new SynInfoQueryAction();
        queryAction.setDataId(dataId);
        queryAction.setDataType("1");
        queryAction.setApiCode(apiCode);
        queryAction.setActionStatus(1);
        queryAction.setActionDate(actionDate);
        queryAction.setDeleteFlag(0);
        queryAction.setCreateTime(new Date());
        queryAction.setUpdateTime(new Date());
        synInfoQueryActionMapper.insert(queryAction);
        return queryAction;
    }
    public void updateActionStatus(Long id, Integer status) {
        SynInfoQueryAction action = new SynInfoQueryAction();
        action.setId(id);
        action.setActionStatus(status);
        synInfoQueryActionMapper.updateByPrimaryKeySelective(action);
    }


}
