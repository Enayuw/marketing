package com.br.marketing.monkey.service.qifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.br.marketing.client.qifu.ResponseData;
import com.br.marketing.client.qifu.callrealtime.CallRealTimeDTO;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeReq;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeResp;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.entity.EventPushData;
import com.br.marketing.mapper.BQifuUploadDataOriginalMapper;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.service.Impl.qifu.enums.QiFuSelectStatusEnum;
import com.br.marketing.strategy.MethodRetryHandlerService;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private static final String ROBOT_EVENT_PUSH = "_robot_event_push";

    @Resource
    private DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;

    @Resource
    private BQifuUploadDataOriginalMapper qiFuUploadDataOriginalMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private QiFuAiEventPushService qiFuAiEventPushService;

    private static final Integer PAGE_SIZE = 50;

    @Override
    public void assembleRealTimeUploadDataOriginal() {

        //分页查找未同步的事件推送数据sync_status = 0
        Long minId = null;
        while (true) {
            List<DrsCustomizeUploadData> drsCustomizeUploadDataList = qiFuAiEventPushService.getDrsCustomizeUploadDataBySyncStatus(0, minId, PAGE_SIZE);
            if (CollectionUtils.isEmpty(drsCustomizeUploadDataList)) {
                break;
            }

            minId = drsCustomizeUploadDataList.get(drsCustomizeUploadDataList.size() - 1).getId();

            //解析事件推送接口原始数据
            for (DrsCustomizeUploadData drsCustomizeUploadData : drsCustomizeUploadDataList) {

                List<BQifuUploadDataOriginal> resultList = new ArrayList<>();

                JSONObject jsonObject = JSONObject.parseObject(drsCustomizeUploadData.getRequestJsonData());
                List<EventPushData> eventPushDataList = jsonObject.getJSONArray("eventList").toJavaList(EventPushData.class);

                if (eventPushDataList != null && !CollectionUtils.isEmpty(eventPushDataList)) {
                    for (EventPushData eventPushData : eventPushDataList) {
                        String serialNo = eventPushData.getSerialNo();

                        //根据serialNo查询明细表
                        List<BQifuUploadDataOriginal> uploadDataOriginalList = qiFuAiEventPushService.getQiFuUploadDataOriginalBySerialNo(serialNo);
                        if (!CollectionUtils.isEmpty(uploadDataOriginalList)) {
                            BQifuUploadDataOriginal bqifuUploadDataOriginal = uploadDataOriginalList.get(0);
                            bqifuUploadDataOriginal.setEventType(eventPushData.getEventType());
                            bqifuUploadDataOriginal.setSerialNo(serialNo);
                            bqifuUploadDataOriginal.setTemplateNo(eventPushData.getTemplateNo());
                            bqifuUploadDataOriginal.setFlowNo(eventPushData.getFlowNo());
                            resultList.add(bqifuUploadDataOriginal);
                        }
                    }
                }
                if (CollectionUtils.isEmpty(resultList)) {
                    updateSyncStatusById(String.valueOf(drsCustomizeUploadData.getId()), 1);
                }else {
                    //先查询外呼信息
                    qiFuAiEventPushService.queryCallMessage(resultList);
                    //在事务中处理数据库操作：插入数据 + 更新状态
                    qiFuAiEventPushService.processBatchData(resultList, drsCustomizeUploadData);
                }
            }
        }
    }

    @Override
    public List<DrsCustomizeUploadData> getDrsCustomizeUploadDataBySyncStatus(Integer syncStatus, Long minId, Integer pageSize) {
        return drsCustomizeUploadDataMapper.getDrsCustomizeUploadDataBySyncStatus(ROBOT_EVENT_PUSH, syncStatus, minId, pageSize);
    }

    @Override
    public List<BQifuUploadDataOriginal> getQiFuUploadDataOriginalBySerialNo(String serialNo) {
        return drsCustomizeUploadDataMapper.getQiFuUploadDataOriginalBySerialNo(ROBOT_EVENT_PUSH, serialNo);
    }

    @Override
    public void updateSyncStatusById(String id, Integer syncStatus) {
        drsCustomizeUploadDataMapper.updateSyncStatusById(ROBOT_EVENT_PUSH, id, syncStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processBatchData(List<BQifuUploadDataOriginal> resultList,
                                 DrsCustomizeUploadData drsCustomizeUploadData) {
        Long updateId = drsCustomizeUploadData.getId();
        try {
            //1. 插入数据
            insertRealTimeData(resultList);

            //2. 更新同步状态为1，确保数据已成功处理
            updateSyncStatusById(String.valueOf(updateId), 1);
            logger.warn("数据处理成功，本批次处理记录id：{}，插入数据数：{}", updateId, resultList.size());
        } catch (Exception e) {
            logger.error("批次数据处理失败，回滚事务。本批次记录id：{}，错误信息：{}", updateId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void insertRealTimeData(List<BQifuUploadDataOriginal> qifuUploadDataOriginalList) {
        for (BQifuUploadDataOriginal qiFuUploadDataOriginal : qifuUploadDataOriginalList) {
            qiFuUploadDataOriginal.setCreateTime(new Date());
            qiFuUploadDataOriginal.setUpdateTime(new Date());
            qiFuUploadDataOriginal.setIsReal(1);
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
            // 有异常，更新select_status为3（重试-接口异常）
            qifuUploadDataOriginalList.forEach(qiFuUploadDataOriginal -> {
                qiFuUploadDataOriginal.setId(null);
                qiFuUploadDataOriginal.setSelectStatus(QiFuSelectStatusEnum.RETRY_INTERFACE_ERROR.getCode());
                qiFuUploadDataOriginal.setCreateTime(new Date());
                qiFuUploadDataOriginal.setUpdateTime(new Date());
            });
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
                originalData.setStatus(0);
                originalData.setSelectStatus(QiFuSelectStatusEnum.QUERY_SUCCESS.getCode());
                originalData.setUpdateTime(new Date());
            }
        }

        logger.warn("事件推送实时查询外呼信息完成，原始数据数量：{}，查询结果数量：{}",
                qifuUploadDataOriginalList.size(), allCallRealTimeList.size());
    }
}
