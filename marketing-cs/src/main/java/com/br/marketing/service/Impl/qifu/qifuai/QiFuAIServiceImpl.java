package com.br.marketing.service.Impl.qifu.qifuai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.qifu.QiFuClients;
import com.br.marketing.client.qifu.ResponseData;
import com.br.marketing.client.qifu.callrealtime.CallRealTimeDTO;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeReq;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeResp;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.service.Impl.qifu.valobj.QiFuSyncStatusEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 已废弃
 */

@Service
@Slf4j
public class QiFuAIServiceImpl implements QiFuAIService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;


    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;


    @Override
    public void queryCallMessage() {

        JSONObject qifuAiCleanConfig = marketingCommonConfig.getQifuAiCleanConfig();
        String tcId = getValueOfJson(qifuAiCleanConfig, "tCid", "");
        List<String> apiCodes = Arrays.asList(getValueOfJson(qifuAiCleanConfig, "cleanApiCode", "3700226").split(","));
        String dataTimeMark = getValueOfJson(qifuAiCleanConfig, "dataTime", "-1");
        LocalDate now = LocalDate.now();
        String nowDay = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String yesterDay = now.minusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<String> receiveDates = new ArrayList<>();
        if ("-1".equals(dataTimeMark)) {
            receiveDates.add(yesterDay);
            receiveDates.add(nowDay);
        } else if ("1".equals(dataTimeMark)) {
            receiveDates.add(nowDay);
        } else {
            receiveDates.add(dataTimeMark);
        }
        Integer pageSize = Integer.valueOf(getValueOfJson(qifuAiCleanConfig, "queryCallPageSize", "10"));
        Integer threadNum = Integer.valueOf(getValueOfJson(qifuAiCleanConfig, "queryCallThreadNum", "1"));
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum, "qiAiQueryCallMsg", 200);
        Boolean actionMark = Boolean.TRUE;
        Long indexId = null;
        while (actionMark) {

            Boolean b = dynamicAction(threadPool);
            if (b) {
                actionMark = Boolean.FALSE;
                continue;
            }
            List<DrsCustomizeUploadData> syncDataList = drsCustomizeUploadDataMapper.getDataOfToBeSync(tcId, apiCodes, receiveDates, pageSize,
                    indexId);
            if (syncDataList.size() <= 0) {
                actionMark = Boolean.FALSE;
                continue;
            }
            indexId = syncDataList.get(syncDataList.size() - 1).getId();
            syncDataList.forEach(uploadData -> {
                threadPool.submit(() -> {
                    try {
                        queryCallHandle(uploadData,tcId);
                    } catch (Exception ex) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode(), ex.getMessage()), ex);
                    }
                });
            });

        }
        shutDownThreadPool(threadPool);
    }

    private void queryCallHandle(DrsCustomizeUploadData uploadData,String tcId) {

        String requestJsonData = uploadData.getRequestJsonData();
        JSONObject jsonObject = JSONObject.parseObject(requestJsonData);
        JSONArray dataList = jsonObject.getJSONArray("dataList");
        List<String> serialNoList = dataList.stream().map(json -> ((JSONObject) json).getString("serialNo")).collect(Collectors.toList());
        List<List<String>> partition = ListUtils.partition(serialNoList, 50);
        List<Result<ResponseData<QryCallRealTimeResp>>> resultList = new ArrayList<>();
        partition.forEach(list -> {
            QryCallRealTimeReq qryCallRealTimeReq = new QryCallRealTimeReq();
            qryCallRealTimeReq.setCallType("AI");
            qryCallRealTimeReq.setRequestNo(UUID.randomUUID().toString());
            qryCallRealTimeReq.setSerialNoList(list);
            Result<ResponseData<QryCallRealTimeResp>> result = methodRetryHandlerService.qryCallRealTime(qryCallRealTimeReq,0);
            resultList.add(result);
        });

        List failResult = resultList.stream().filter(result -> !ResultCode.SUCCESS.getValue().equals(result.getCode())).collect(Collectors.toList());
        //有异常，直接返回
        if (!CollectionUtils.isEmpty(failResult)) {
            return;
        }
        List<CallRealTimeDTO> detailList = new ArrayList<>();
        resultList.forEach(responseDataResult -> {
            List<CallRealTimeDTO> callRealTimeDTOList = responseDataResult.getData().getData().getT().getDataDetails();
            detailList.addAll(callRealTimeDTOList);
        });

        drsCustomizeUploadDataMapper.updateExtendAndStatusById(tcId, uploadData.getId(), QiFuSyncStatusEnum.QUERY_COMPLETE.getValue(),
                JSON.toJSONString(detailList));

    }

    private String getValueOfJson(JSONObject jo, String key, String defaultValue) {
        if (jo == null || ObjectUtils.isEmpty(jo.getString(key))) {
            return defaultValue;
        }
        return jo.getString(key);
    }

    public void shutDownThreadPool(ThreadPoolExecutor threadPool) {
        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("360AI查询外呼信息 等待线程池结束");
            }
        } catch (InterruptedException e) {
            log.error("360AI查询外呼信息 线程池关闭异常,直接关闭线程池", e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private Boolean dynamicAction(ThreadPoolExecutor executor) {
        JSONObject qifuAiCleanConfig = marketingCommonConfig.getQifuAiCleanConfig();
        Boolean isPause = qifuAiCleanConfig.getBoolean("queryCallisPause");
        if (isPause == null || isPause) {
            return Boolean.TRUE;
        }
        if (StringUtils.isNotBlank(qifuAiCleanConfig.getString("queryCallThreadNum"))) {
            Integer threadNum = Integer.valueOf(qifuAiCleanConfig.getString("queryCallThreadNum"));
            if (executor.getCorePoolSize() != threadNum.intValue()) {
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(executor, threadNum);
            }
        }
        return Boolean.FALSE;
    }


}

