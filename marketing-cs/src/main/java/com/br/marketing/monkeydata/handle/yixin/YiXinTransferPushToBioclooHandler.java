package com.br.marketing.monkeydata.handle.yixin;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.monkeydata.handle.yixin.sole.YiXinTransferPushRedisSoleProcessor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.br.common.log.AlertLog;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.baiying.ByApiServiceClient;
import com.br.marketing.client.baiying.input.BlacklistDataDTO;
import com.br.marketing.client.baiying.input.ReqBlacklistDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.yixin.YiXinCondition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * 宜信转化过滤推送百应
 */
@Service
@Slf4j
public class YiXinTransferPushToBioclooHandler extends IMonkeyDataHandle<MarketingTransferSyncUser, MarketingTransferSyncUser, YiXinCondition> {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private YiXinTransferPushRedisSoleProcessor transferPushRedisSoleProcessor;

    @Resource
    private ByApiServiceClient byApiServiceClient;

    private final static String TITLE = "【宜信转化过滤推送百应】";

    @Override
    public Result<IterationResult<MarketingTransferSyncUser, YiXinCondition>> getInputData(YiXinCondition condition) {
        return null;
    }

    @Override
    public Result<?> customizedAction(YiXinCondition condition) {
        Result<?> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());

        ThreadPoolExecutor processPool = BrExecutors.getThreadPool(12, 12, 20);
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(24, 24, new SynchronousQueue<>());

        List<Future<Result<List<MarketingTransferSyncUser>>>> futureList = new ArrayList<>();
        Integer pageSize = condition.getPageSize();
        String apiCode = condition.getApiCode();
        String requestData = condition.getRequestData();
        String synApiCode = condition.getSynApiCode();
        String priority = condition.getPriority();
        String extendSql = assembleExtendSql(priority);
        String tCid = tableCreateService.getTcId(apiCode);

        Long indexId = null;
        while (true) {
            // 循环获取条件数据，每次pageSize条
            final List<MarketingTransferSyncUser> pageList =
                marketingTransferSyncUserMapper.getYxCustNumsByRequestDate(tCid, apiCode, requestData, extendSql, indexId, pageSize);

            if (CollectionUtils.isEmpty(pageList)) {
                break;
            }

            indexId = pageList.get(pageList.size() - 1).getId();

            setThreadPoolParam(processPool, pushPool);

            log.warn(TITLE + "action, 加入processPool");
            futureList.add(processPool.submit(() -> processData(pageList, condition, pushPool)));

        }

        for (Future<Result<List<MarketingTransferSyncUser>>> future : futureList) {
            try {
                future.get(1, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage(), TITLE), e);
                // future.cancel(true);
                result.setCode(ResultCode.FAIL.getValue());
            }
        }

        long taskCount = -1;
        processPool.shutdown();
        try {
            while (!processPool.awaitTermination(30, TimeUnit.SECONDS)) {
                long completedTask2Count = processPool.getCompletedTaskCount();
                if (taskCount == completedTask2Count) {
                    result.setCode(ResultCode.FAIL.getValue());
                    log.warn(TITLE + "业务线程等待超时, {}, {}", apiCode, requestData);
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage(), TITLE), e);
            result.setCode(ResultCode.FAIL.getValue());
            Thread.currentThread().interrupt();
        }

        taskCount = -1;
        pushPool.shutdown();
        try {
            while (!pushPool.awaitTermination(10, TimeUnit.SECONDS)) {
                long completedTask2Count = pushPool.getCompletedTaskCount();
                if (taskCount == completedTask2Count) {
                    result.setCode(ResultCode.FAIL.getValue());
                    log.warn(TITLE + "推送线程等待超时, {}, {}", apiCode, requestData);
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage(), TITLE), e);
            result.setCode(ResultCode.FAIL.getValue());
            Thread.currentThread().interrupt();
        }
        return result;
    }

    @Override
    public Result<List<MarketingTransferSyncUser>> processData(List<MarketingTransferSyncUser> inList) {
        return null;
    }

    public Result<List<MarketingTransferSyncUser>> processData(List<MarketingTransferSyncUser> pageList, YiXinCondition condition,
        ThreadPoolExecutor pushPool) {
        Result<List<MarketingTransferSyncUser>> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());
        log.warn(TITLE + "processData开始");

        int processSize = pageList.size();
        long startTime = System.currentTimeMillis();

        try {
            // pageParam
            String apiCode = condition.getApiCode();
            String requestData = condition.getRequestData();
            String synApiCode = condition.getSynApiCode();

            // valid period
            Set<String> custNumSets = pageList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> custNumToSyncUserBoMap =
                transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSets, synApiCode, requestData);

            // 未获取到上传数据
            if (CollectionUtils.isEmpty(custNumToSyncUserBoMap)) {
                log.warn(TITLE + "未获取到上传数据或未配置有效期, apiCode: {}, requestData: {}", apiCode, requestData);
                return result;
            }

            // 过滤掉非百可录标识的上传数据
            custNumToSyncUserBoMap.forEach((String key, SyncUserValidityPeriodsBO periodsBO) -> {
                List<MarketingSyncUser> filtered = periodsBO.getSyncUsers().stream().filter((MarketingSyncUser syncUser) -> {
                    String reserveField1 = syncUser.getReserveField1();
                    return StringUtils.isNotEmpty(reserveField1) && JSONObject.isValid(reserveField1)
                        && "2".equals(JSONObject.parseObject(reserveField1).getString("resourceChannel"));
                }).collect(Collectors.toList());
                periodsBO.setSyncUsers(filtered);
            });

            List<MarketingTransferSyncUser> periodList = pageList.stream().filter((MarketingTransferSyncUser data) -> {
                String custNum = data.getCustNum();
                if (custNumToSyncUserBoMap.get(custNum) == null || CollectionUtil.isEmpty(custNumToSyncUserBoMap.get(custNum).getSyncUsers())) {
                    return false;
                }
                MarketingSyncUser marketingSyncUser = custNumToSyncUserBoMap.get(custNum).getSyncUsers().get(0);
                data.setReserveField2(marketingSyncUser.getCellMd5());
                return true;
            }).collect(Collectors.toList());

            List<MarketingTransferSyncUser> pushList = periodList;
            if (CollectionUtils.isEmpty(pushList)) {
                return result;
            }

            // distribute去重 custNum + distribute_date
            transferPushRedisSoleProcessor.process(pushList, condition, DistributeTypeEnum.YIXIN_TRANSFER_PUSH_BIOCLOO.getValue());

            Result<?> resultAction = resultAction(pushList, condition, pushPool);
            result.setCode(resultAction.getCode());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        long endTime = System.currentTimeMillis();
        log.warn(TITLE + "processData结束, 量级{}, 耗时{}", processSize, (endTime - startTime));
        return result;
    }

    @Override
    public Result<?> resultAction(List<MarketingTransferSyncUser> outputDataList) {
        return null;
    }

    public Result<?> resultAction(List<MarketingTransferSyncUser> outputDataList, YiXinCondition condition, ThreadPoolExecutor pushPool) {
        Result<Object> result = new Result<>();
        if (CollectionUtils.isEmpty(outputDataList)) {
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }

        int processSize = outputDataList.size();
        long startTime = System.currentTimeMillis();

        Map<String, Object> pushConfigMap = marketingCommonConfig.getYiXinTransferPushBaiYingPush();
        int pushSize = pushConfigMap.get("pushPartSize") != null ? Integer.parseInt(String.valueOf(pushConfigMap.get("pushPartSize"))) : 500;
        String pushMethod = pushConfigMap.get("pushMethod") != null ? String.valueOf(pushConfigMap.get("pushMethod")) : "blackData";

        int size = outputDataList.size();
        int count = 0;
        List<BlacklistDataDTO> pushBioclooList = new ArrayList<>();
        for (MarketingTransferSyncUser transferSyncUser : outputDataList) {
            BlacklistDataDTO blackBlklistDataDTO = new BlacklistDataDTO();
            blackBlklistDataDTO.setCaseNum(transferSyncUser.getCustNum());
            blackBlklistDataDTO.setExpireDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).concat(" 23:59:59"));
            blackBlklistDataDTO.setPhone(transferSyncUser.getReserveField2());
            pushBioclooList.add(blackBlklistDataDTO);
            count++;
            if (pushBioclooList.size() == pushSize || size == count) {
                List<BlacklistDataDTO> finalList = pushBioclooList;
                pushPool.execute(() -> {

                    // 紧急需求，增加了推送百可录逻辑（后续逻辑变更请注意）
                    ReqBlacklistDTO reqBklBlacklistDTO = new ReqBlacklistDTO();
                    reqBklBlacklistDTO.setMethod(pushMethod);
                    reqBklBlacklistDTO.setApiCode(condition.getSynApiCode());
                    reqBklBlacklistDTO.setData(finalList);
                    byApiServiceClient.pushDataToBiocloo(reqBklBlacklistDTO, 0);
                });
                pushBioclooList = new ArrayList<>();
            }
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        long endTime = System.currentTimeMillis();
        log.warn(TITLE + "resultAction, 量级{}, 耗时{}", processSize, (endTime - startTime));
        return result;
    }

    /**
     * 配置线程池参数
     */
    private void setThreadPoolParam(ThreadPoolExecutor processPool, ThreadPoolExecutor pushPool) {
        Map<String, Integer> threadPoolConfig = marketingCommonConfig.getYiXinTransferPushBaiYingThreadPool();
        int processPoolSize = threadPoolConfig.get("processPoolSize");
        int pushPoolSize = threadPoolConfig.get("pushPoolSize");

        if (ObjectUtils.isEmpty(processPoolSize) || processPoolSize < 1) {
            processPoolSize = Runtime.getRuntime().availableProcessors() * 10;
        }
        if (ObjectUtils.isEmpty(pushPoolSize) || pushPoolSize < 1) {
            pushPoolSize = Runtime.getRuntime().availableProcessors() * 10;
        }

        int curProcessPoolSize = processPool.getCorePoolSize();
        int curPushPoolSize = pushPool.getCorePoolSize();

        if (processPoolSize != curProcessPoolSize) {
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(processPool, processPoolSize);
        }

        if (pushPoolSize != curPushPoolSize) {
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pushPool, pushPoolSize);
        }
    }

    private String assembleExtendSql(String priority) {
        String extendSql = "";
        switch (priority) {
            case "1":
                extendSql = "and if_apply ='1' and apply_result ='0' "
                    + "AND (reserve_field1 -> '$.transformType' != '1' or reserve_field1 -> '$.transformType' IS NULL)";
                break;
            case "2":
                extendSql = "and if_apply ='1' and apply_result ='2' "
                    + "AND (reserve_field1 -> '$.transformType' != '1' or reserve_field1 -> '$.transformType' IS NULL)";
                break;
            case "3":
                extendSql = "and if_lent ='0' " + "AND (reserve_field1 -> '$.transformType' != '1' or reserve_field1 -> '$.transformType' IS NULL) "
                    + "AND reserve_field1->'$.applyLoan' = '1'";
                break;
            case "4":
                extendSql = "and if_lent ='1' " + "AND (reserve_field1 -> '$.transformType' != '1' or reserve_field1 -> '$.transformType' IS NULL) "
                    + "AND reserve_field1->'$.applyLoan' = '1' and reserve_field1 ->> '$.availableAmount' < 2000.00";
                break;
            default:
                extendSql = "and id < 0";
        }
        return extendSql;
    }
}
