package com.br.marketing.service.Impl.qifu;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.qifu.*;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.qifu.QiFuCuWanJianBatQryUserRealDto;
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
import java.util.*;
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

    ThreadPoolExecutor dbActionPool = BrExecutors.getThreadPool(10, 10);

    private Integer PARTITION_SIZE = 50;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private QiFuClients qiFuClients;

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private SynInfoQueryActionMapper synInfoQueryActionMapper;

    public Result<Map<String, Object>> action(Page2Condition<QiFuCuWanJianBatQryUserRealDto> condition) {
        return scanData(condition);
    }
    public Result<Map<String, Object>> scanData(Page2Condition<QiFuCuWanJianBatQryUserRealDto> condition) {
        Result result = new Result().failure();
        Map<String, Object> data= new HashMap<>();

        QiFuCuWanJianBatQryUserRealDto param = condition.getParam();
        String apiCode = param.getApiCode();
        List<Integer> statusList = param.getStatusList();
        String bizDate = param.getBizDate();
        String actionDate = LocalDate.now().toString();
        Integer pageSize = condition.getPageSize();

        try{
            // 循环获取条件数据，每次pageSize条
            Map<String, String> marketingTimeInterval = calculateTimeInterval(bizDate);
            String createTimeStart = marketingTimeInterval.get("createTimeStart");
            String createTimeEnd = marketingTimeInterval.get("createTimeEnd");
            List<MarketingSyncInfo> marketingSyncInfoList = marketingSyncInfoMapper.querySynInfoWithActiontikv_(apiCode
                    , statusList, actionDate, createTimeStart, createTimeEnd, pageSize);
            if (CollectionUtils.isEmpty(marketingSyncInfoList)) {
                log.warn(TITLE+"scanData, 未获取到数据");
                data.put("hasScanData", "0");
                return new Result().success().setDate(data);
            }
            log.warn(TITLE + "scanData 获取到数据, size: {}", marketingSyncInfoList.size());

            for(MarketingSyncInfo marketingSyncInfo : marketingSyncInfoList) {
                Long dataId = marketingSyncInfo.getId();
                String taskId = marketingSyncInfo.getCusBatch();
                log.warn(TITLE + "scanData 获取到数据, dataId: {}, taskId: {}", dataId, taskId);

                // saveAction
                SynInfoQueryAction queryAction = saveAction(dataId, apiCode, actionDate);

                // marketingSyncUserList
                List<MarketingSyncUser> marketingSyncUserList = marketingSyncUserMapper.getSyncUserByRequestBatch(apiCode, marketingSyncInfo.getRequestBatch());

                // actionDataList
                Result<Map<String, Object>> actionResult = actionDataList(apiCode, taskId, marketingSyncUserList);

                // updateActionStatus
                if (actionResult != null && actionResult.isSuccess()) {
                    updateActionStatus(queryAction.getId(), 2);
                    log.warn(TITLE + "今日更新成功, apiCode:{}, bizDate:{}", apiCode, bizDate);
                }
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(), TITLE+ e.getMessage()));
        }
        return result;
    }

    private Result<Map<String, Object>> actionDataList(String apiCode, String taskId, List<MarketingSyncUser> dataList) {
        Result result = new Result().failure();
        Integer threadPoolSize = Integer.parseInt(String.valueOf(marketingCommonConfig.getQiFuCuWanJianBatQryUserRealConfigParams().get("threadPoolSize")));
        Integer partitionSize = Integer.parseInt(String.valueOf(marketingCommonConfig.getQiFuCuWanJianBatQryUserRealConfigParams().get("partitionSize")));
        dbActionPool.setCorePoolSize(threadPoolSize);
        dbActionPool.setMaximumPoolSize(threadPoolSize);
        PARTITION_SIZE = partitionSize;

        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        List<List<MarketingSyncUser>> dataPartitions = Lists.partition(dataList, PARTITION_SIZE);
        for (List<MarketingSyncUser> partition : dataPartitions) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    actionPartition(apiCode, taskId, partition);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(), TITLE + "分页数据处理异常"));
                }
            }, dbActionPool));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.warn(TITLE + "actionTaskDataList, apiCode: {}, taskId: {}");
        return result.success();
    }

    @RetryMethod(retryNowNum = 3, isOrNoDbRetry = true)
    private void actionPartition(String apiCode, String taskId, List<MarketingSyncUser> partition) {
        List<RealDataesReq> realDataes = new ArrayList<>();
        Map<String, Long> custNumToIdMap = new HashMap<>();
        for (MarketingSyncUser marketingSyncUser : partition) {
            RealDataesReq realDataesReq = new RealDataesReq();
            realDataesReq.setUniqueReqNo(marketingSyncUser.getCustNum());
            realDataesReq.setMobileMd5(marketingSyncUser.getCellMd5());
            realDataes.add(realDataesReq);
            Long id = marketingSyncUser.getId();
            custNumToIdMap.put(marketingSyncUser.getCustNum(), id);
        }

        QrySleepUserRealMessageReq qrySleepUserRealMessageReq = new QrySleepUserRealMessageReq();
        String uuid = UUID.randomUUID().toString();
        qrySleepUserRealMessageReq.setRequestNo(uuid);
        qrySleepUserRealMessageReq.setBatchNo(taskId);
        qrySleepUserRealMessageReq.setInitiatingType("noArt");
        qrySleepUserRealMessageReq.setPartner("bairong");
        qrySleepUserRealMessageReq.setRealDataes(realDataes);
        Result<ResponseData<QrySleepUserRealMessageResp>> dataResult = qiFuClients.qryUserRealMessageUrl(qrySleepUserRealMessageReq);
        log.warn(TITLE + "返回结果, dataResult{}", JSONObject.toJSONString(dataResult));

        if (ResultCode.SUCCESS.getValue().equals(dataResult.getCode())) {
            ResponseData<QrySleepUserRealMessageResp> data = dataResult.getData();
            QrySleepUserRealMessageResp qrySleepUserRealMessageResp = data.getData().getT();
            List<QryUserRealMessage> realDetails = qrySleepUserRealMessageResp.getRealDetails();
            for (QryUserRealMessage qryUserRealMessage : realDetails) {
                String custNum = qryUserRealMessage.getUniqueReqNo();
                // String mobileMd5 = qryUserRealMessage.getMobileMd5();
                Object userMessageRes = qryUserRealMessage.getUserMessageRes();
                if (userMessageRes == null) {
                    continue;
                }
                JSONObject userMessageJo = JSONObject.parseObject(userMessageRes.toString());
                if (userMessageJo == null) {
                    continue;
                }

                List<Map<String, String>> extendList = assembleExtendList(userMessageJo);
                // update
                Long id = custNumToIdMap.get(custNum);
                marketingSyncUserMapper.updateExtend(apiCode, custNum, extendList, id, null);
            }
        }
    }

    private List<Map<String, String>> assembleExtendList(JSONObject userMessageJo){
        String name = userMessageJo.getString("name");
        if(StringUtils.isEmpty(name)){
            name = "";
        }
        String sex = userMessageJo.getString("sex");
        if(StringUtils.isEmpty(sex)){
            sex = "";
        }
        String gender;
        switch (sex){
            case "F": gender="0"; break;
            case "M": gender="1"; break;
            default: gender="";
        }

        // extendList
        List<Map<String, String>> extendList = new ArrayList<>();
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("key", "cusName");
        nameMap.put("value", name);
        Map<String, String> sexMap = new HashMap<>();
        sexMap.put("key", "gender");
        sexMap.put("value", gender);

        extendList.add(nameMap);
        extendList.add(sexMap);
        return extendList;
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
