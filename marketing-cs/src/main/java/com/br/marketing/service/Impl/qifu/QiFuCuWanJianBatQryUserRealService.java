package com.br.marketing.service.Impl.qifu;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.client.qifu.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.qifu.QiFuCuWanJianBatQryUserRealDto;
import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.ParseException;
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

    public Result<Map<String, Object>> action(Page2Condition<QiFuCuWanJianBatQryUserRealDto> condition) {
        return scanData(condition);
    }
    public Result<Map<String, Object>> scanData(Page2Condition<QiFuCuWanJianBatQryUserRealDto> condition) {
        Result result = new Result().failure();
        Map<String, Object> data= new HashMap<>();

        QiFuCuWanJianBatQryUserRealDto param = condition.getParam();
        String apiCode = param.getApiCode();
        List<Integer> statusList = param.getStatusList();
        String actionData = param.getActionData();
        Integer pageSize = condition.getPageSize();

        try{
            // 循环获取条件数据，每次pageSize条
            Map<String, String> marketingTimeInterval = calculateTimeInterval(actionData);
            String createTimeStart = marketingTimeInterval.get("createTimeStart");
            String createTimeEnd = marketingTimeInterval.get("createTimeEnd");
            List<MarketingSyncInfo> marketingSyncInfoList = marketingSyncInfoMapper.querySynInfoWithAction(apiCode, statusList, createTimeStart, createTimeEnd);
            if (CollectionUtils.isEmpty(marketingSyncInfoList)) {
                log.warn(TITLE+"scanData, 未获取到数据");
                data.put("hasScanData", "0");
                return new Result().success().setDate(data);
            }
            log.warn(TITLE + "scanData 获取到数据, 条数{}", marketingSyncInfoList.size());

            MarketingSyncInfo marketingSyncInfo = marketingSyncInfoList.get(0);
            String taskId = marketingSyncInfo.getCusBatch();
            List<MarketingSyncUser> marketingSyncUserList = marketingSyncUserMapper.getSyncUserByRequestBatch(apiCode, marketingSyncInfo.getRequestBatch());

            // process submit data
            actionDataList(apiCode, taskId, marketingSyncUserList);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(), TITLE+ e.getMessage()));
            Thread.currentThread().interrupt();
        }
        return result;
    }

    private void actionDataList(String apiCode, String taskId, List<MarketingSyncUser> dataList) {
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
    }

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
            realDataes.add(realDataesReq);
        }

        QrySleepUserRealMessageReq qrySleepUserRealMessageReq = new QrySleepUserRealMessageReq();
        String uuid = UUID.randomUUID().toString();
        qrySleepUserRealMessageReq.setRequestNo(uuid);
        qrySleepUserRealMessageReq.setBatchNo(taskId);
        qrySleepUserRealMessageReq.setInitiatingType("noArt");
        qrySleepUserRealMessageReq.setPartner("bairong");
        qrySleepUserRealMessageReq.setRealDataes(realDataes);
        Result<ResponseData<QrySleepUserRealMessageResp>> dataResult = qiFuClients.qrySleepUserRealMessage(qrySleepUserRealMessageReq);
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
        String sex = userMessageJo.getString("sex");
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
        nameMap.put("key", "gender");
        nameMap.put("value", gender);

        extendList.add(nameMap);
        extendList.add(sexMap);
        return extendList;
    }

    private Map<String, String> calculateTimeInterval(String actionDate) throws ParseException {
        Map<String, String> res = new HashMap<>();
        Date bizDate = DateUtils.parse(actionDate, "yyyyMMdd");
        Date endDate = new Date(bizDate.getTime() + 86400000L);

        String createTimeStart = DateUtils.format(bizDate, "yyyy-MM-dd 00:00:00");
        String createTimeEnd = DateUtils.format(endDate, "yyyy-MM-dd 00:00:00");

        res.put("createTimeStart", createTimeStart);
        res.put("createTimeEnd", createTimeEnd);
        return res;
    }


}
