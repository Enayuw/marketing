package com.br.marketing.check.service.Impl.qifu;

import com.alibaba.fastjson.JSON;
import com.br.marketing.check.service.qifu.QiFuAiEventPushService;
import com.br.marketing.client.qifu.ResponseData;
import com.br.marketing.client.qifu.callrealtime.CallRealTimeDTO;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeReq;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeResp;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.mapper.BQifuUploadDataOriginalMapper;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.strategy.MethodRetryHandlerService;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName QiFuAiEventPushServiceImpl
 * @Author hang.zhou
 * @Date 2025/11/17
 */
@Service
public class QiFuAiEventPushServiceImpl implements QiFuAiEventPushService {

    private static final Logger logger = LoggerFactory.getLogger(QiFuAiEventPushServiceImpl.class);

    @Resource
    private DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;

    @Resource
    private BQifuUploadDataOriginalMapper qiFuUploadDataOriginalMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public List<DrsCustomizeUploadData> getDrsCustomizeUploadDataBySyncStatus(Integer syncStatus) {
        return drsCustomizeUploadDataMapper.getDrsCustomizeUploadDataBySyncStatus("robot_event_push", syncStatus);
    }

    @Override
    public List<BQifuUploadDataOriginal> getQiFuUploadDataOriginalBySerialNo(String serialNo) {
        return drsCustomizeUploadDataMapper.getQiFuUploadDataOriginalBySerialNo("robot_event_push", serialNo);
    }

    @Override
    public void insertRealTimeData(List<BQifuUploadDataOriginal> qifuUploadDataOriginalList) {
        for (BQifuUploadDataOriginal qiFuUploadDataOriginal : qifuUploadDataOriginalList) {
            qiFuUploadDataOriginalMapper.insertSelective(qiFuUploadDataOriginal);
        }
    }

    @Override
    public void queryCallMessage(List<BQifuUploadDataOriginal> qifuUploadDataOriginalList) {
        List<String> serialNoList = qifuUploadDataOriginalList.stream()
                .map(BQifuUploadDataOriginal::getSerialNo).collect(Collectors.toList());

        List<List<String>> partitions = ListUtils.partition(serialNoList, 50);
        List<Result<ResponseData<QryCallRealTimeResp>>> resultList = new ArrayList<>();

        //调用奇富查询外呼信息接口
        for (List<String> partition : partitions) {
            QryCallRealTimeReq qryCallRealTimeReq = new QryCallRealTimeReq();
            qryCallRealTimeReq.setRequestNo(UUID.randomUUID().toString());
            qryCallRealTimeReq.setCallType("AI");
            qryCallRealTimeReq.setSerialNoList(partition);

            Result<ResponseData<QryCallRealTimeResp>> responseDataResult = methodRetryHandlerService.qryCallRealTime(qryCallRealTimeReq, null);
            resultList.add(responseDataResult);
        }

        //处理外呼接口响应
        List<Result<ResponseData<QryCallRealTimeResp>>> failureList = resultList.stream()
                .filter(responseDataResult -> !ResultCode.SUCCESS.getValue().equals(responseDataResult.getCode()))
                .collect(Collectors.toList());

        //存在失败请求，直接返回
        if (!failureList.isEmpty()) {
            logger.warn("事件推送实时查询外呼接口异常，失败数量：{}", failureList.size());
            return;
        }

        //收集所有查询结果的CallRealTimeDTO列表
        List<CallRealTimeDTO> allCallRealTimeList = new ArrayList<>();
        for (Result<ResponseData<QryCallRealTimeResp>> result : resultList) {
            if (result.getData() != null 
                && result.getData().getData() != null 
                && result.getData().getData().getT() != null 
                && !CollectionUtils.isEmpty(result.getData().getData().getT().getDataDetails())) {
                allCallRealTimeList.addAll(result.getData().getData().getT().getDataDetails());
            }
        }

        //根据serialNo创建Map
        Map<String, CallRealTimeDTO> callRealTimeMap = allCallRealTimeList.stream()
                .filter(dto -> dto.getSerialNo() != null)
                .collect(Collectors.toMap(CallRealTimeDTO::getSerialNo, dto -> dto, (v1, v2) -> v1));

        //遍历原始数据，匹配查询结果并存入extend字段
        for (BQifuUploadDataOriginal originalData : qifuUploadDataOriginalList) {
            String serialNo = originalData.getSerialNo();
            if (serialNo != null && callRealTimeMap.containsKey(serialNo)) {
                CallRealTimeDTO callRealTimeDTO = callRealTimeMap.get(serialNo);
                //将查询结果转换为JSON字符串存入extend字段
                originalData.setExtend(JSON.toJSONString(callRealTimeDTO));
            }
        }

        logger.warn("事件推送实时查询外呼信息完成，原始数据数量：{}，查询结果数量：{}",
                qifuUploadDataOriginalList.size(), allCallRealTimeList.size());
    }
}
