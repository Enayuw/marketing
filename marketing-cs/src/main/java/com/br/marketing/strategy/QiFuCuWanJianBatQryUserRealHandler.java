package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.qifu.*;
import com.br.marketing.client.robotaiapi.input.InterfaceData;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 360促完件用户信息批量查询
 *
 * @Author lixiang
 * @Date 2024-10-19
 */
@Service
@Slf4j
public class QiFuCuWanJianBatQryUserRealHandler extends AbstractExternalInterfaceHandler<InterfaceData<MarketingSyncUser>> {

    private final static String TITLE = "【360促完件用户信息批量查询-清洗数据更新】";

    ThreadPoolExecutor dbActionPool = BrExecutors.getThreadPool(10, 10);

    private Integer PARTITION_SIZE = 50;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private QiFuClients qiFuClients;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public JSONObject call(List<InterfaceData<MarketingSyncUser>> interfaceDataList, ProcessHandlerContext context) {
        // interfaceDataList
        if (CollectionUtils.isEmpty(interfaceDataList)) {
            return null;
        }
        //apiCode
        String apiCode = interfaceDataList.get(0).getData().getApiCode();

        // taskMap
        Map<String, List<InterfaceData<MarketingSyncUser>>> taskMap = interfaceDataList.stream()
                .collect(Collectors.groupingBy((InterfaceData<MarketingSyncUser> data) -> {
            String taskId = data.getData().getCusBatch();
            return taskId;
        }, LinkedHashMap::new, Collectors.toList()));

        // taskId keySet
        Set<String> keySet = taskMap.keySet();
        for (String taskId : keySet) {
            List<InterfaceData<MarketingSyncUser>> taskInterfaceDataList = taskMap.get(taskId);
            actionTaskDataList(apiCode, taskId, taskInterfaceDataList);
        }
        return null;
    }

    private void actionTaskDataList(String apiCode, String taskId, List<InterfaceData<MarketingSyncUser>> taskInterfaceDataList) {
        Integer threadPoolSize = Integer.parseInt(String.valueOf(marketingCommonConfig.getQiFuCuWanJianBatQryUserRealConfigParams().get("threadPoolSize")));
        Integer partitionSize = Integer.parseInt(String.valueOf(marketingCommonConfig.getQiFuCuWanJianBatQryUserRealConfigParams().get("partitionSize")));
        dbActionPool.setCorePoolSize(threadPoolSize);
        dbActionPool.setMaximumPoolSize(threadPoolSize);
        PARTITION_SIZE = partitionSize;

        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        List<List<InterfaceData<MarketingSyncUser>>> dataPartitions = Lists.partition(taskInterfaceDataList, PARTITION_SIZE);
        for (List<InterfaceData<MarketingSyncUser>> partition : dataPartitions) {
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

    private void actionPartition(String apiCode, String taskId, List<InterfaceData<MarketingSyncUser>> partition) {
        List<RealDataesReq> realDataes = new ArrayList<>();
        Map<String, Long> custNumToIdMap = new HashMap<>();
        for (InterfaceData<MarketingSyncUser> interfaceData : partition) {
            MarketingSyncUser marketingSyncUser = interfaceData.getData();
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

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.QIFU_CUWANJIAN_BAT_QRY_USER_REAL;
    }


}
