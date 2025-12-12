package com.br.marketing.monkey.service.qifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.client.qifu.ResponseData;
import com.br.marketing.client.qifu.callrealtime.CallRealTimeDTO;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeReq;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeResp;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.entity.EventPushData;
import com.br.marketing.mapper.BQifuUploadDataOriginalMapper;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.service.Impl.qifu.enums.QiFuDataTypeEnum;
import com.br.marketing.service.Impl.qifu.enums.QiFuProcessStatusEnum;
import com.br.marketing.service.Impl.qifu.enums.QiFuSelectStatusEnum;
import com.br.marketing.service.Impl.qifu.enums.QiFuSyncStatusEnum;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @ClassName QiFuAiEventPushServiceImpl
 * @Author hang.zhou
 * @Date 2025/11/17
 */
@Service
@Slf4j
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

    @Resource
    private TrackingService trackingService;

    private static final Integer PAGE_SIZE = 50;

    @Override
    public void assembleRealTimeUploadDataOriginal() {

        //分页查找未同步的事件推送数据sync_status = 0
        Long minId = null;
        AtomicLong total = new AtomicLong(0L);
        String apiCode = "";
        while (true) {
            List<DrsCustomizeUploadData> drsCustomizeUploadDataList =
                    qiFuAiEventPushService.getDrsCustomizeUploadDataBySyncStatus(QiFuSyncStatusEnum.UN_SYNC.getCode(), minId, PAGE_SIZE);
            if (CollectionUtils.isEmpty(drsCustomizeUploadDataList)) {
                break;
            }

            minId = drsCustomizeUploadDataList.get(drsCustomizeUploadDataList.size() - 1).getId();

            try {
                total.addAndGet(drsCustomizeUploadDataList.size());
                apiCode = drsCustomizeUploadDataList.get(0).getApiCode();
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

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
                    updateSyncStatusById(String.valueOf(drsCustomizeUploadData.getId()), QiFuSyncStatusEnum.SYNC.getCode());
                } else {
                    //先查询外呼信息
                    List<BQifuUploadDataOriginal> queryedtList = qiFuAiEventPushService.queryCallMessage(resultList);
                    //在事务中处理数据库操作：插入数据 + 更新状态
                    qiFuAiEventPushService.processBatchData(queryedtList, drsCustomizeUploadData);
                }
            }
        }

        try {
            JSONObject condition = new JSONObject();
            condition.put("syncStatus", QiFuSyncStatusEnum.UN_SYNC.getCode());
            trackingService.trackBusinessLog(DataFlowDirection.IN
                    , apiCode
                    , "奇富ai事件推送实时数据查询"
                    , "b_drs_customize_upload_data"+ROBOT_EVENT_PUSH
                    , JSON.toJSONString(condition)
                    , total.get()
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
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
            updateSyncStatusById(String.valueOf(updateId), QiFuSyncStatusEnum.SYNC.getCode());
            logger.warn("数据处理成功，本批次处理记录id：{}，插入数据数：{}", updateId, resultList.size());
        } catch (Exception e) {
            logger.error("批次数据处理失败，回滚事务。本批次记录id：{}，错误信息：{}", updateId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void insertRealTimeData(List<BQifuUploadDataOriginal> qifuUploadDataOriginalList) {
        for (BQifuUploadDataOriginal qiFuUploadDataOriginal : qifuUploadDataOriginalList) {
            qiFuUploadDataOriginal.setReceiveDate(DateUtils.format(new Date(), "yyyy-MM-dd"));
            qiFuUploadDataOriginal.setCreateTime(new Date());
            qiFuUploadDataOriginal.setUpdateTime(new Date());
            qiFuUploadDataOriginal.setIsReal(QiFuDataTypeEnum.REALTIME.getCode());
            qiFuUploadDataOriginalMapper.insertSelective(qiFuUploadDataOriginal);
        }
    }

    @Override
    public List<BQifuUploadDataOriginal> queryCallMessage(List<BQifuUploadDataOriginal> qifuUploadDataOriginalList) {
        List<BQifuUploadDataOriginal> resultList = new ArrayList<>();

        List<List<BQifuUploadDataOriginal>> partitions = ListUtils.partition(qifuUploadDataOriginalList, 50);
        int totalProcessed = 0;
        int totalSuccess = 0;

        //调用奇富查询外呼信息接口，在每个partition循环内更新状态
        for (List<BQifuUploadDataOriginal> partition : partitions) {
            List<String> serialNoList = partition.stream()
                    .map(BQifuUploadDataOriginal::getSerialNo)
                    .collect(Collectors.toList());
            QryCallRealTimeReq qryCallRealTimeReq = new QryCallRealTimeReq();
            qryCallRealTimeReq.setRequestNo(UUID.randomUUID().toString());
            qryCallRealTimeReq.setCallType("AI");
            qryCallRealTimeReq.setSerialNoList(serialNoList);

            Result<ResponseData<QryCallRealTimeResp>> responseDataResult = methodRetryHandlerService.qryCallRealTime(qryCallRealTimeReq, null);

            //遍历原始数据，处理属于当前partition的数据
            for (BQifuUploadDataOriginal originalData : partition) {
                String serialNo = originalData.getSerialNo();
                totalProcessed++;

                if (!ResultCode.SUCCESS.getValue().equals(responseDataResult.getCode())) {
                    //接口调用失败，更新错误状态
                    originalData.setId(null);
                    originalData.setStatus(null);
                    originalData.setSelectStatus(QiFuSelectStatusEnum.RETRY_INTERFACE_ERROR.getCode());
                    originalData.setCreateTime(new Date());
                    originalData.setUpdateTime(new Date());
                    resultList.add(originalData);
                } else {
                    //接口调用成功，查找响应结果中是否有匹配的数据
                    List<CallRealTimeDTO> callRealTimeList = new ArrayList<>();
                    if (responseDataResult.getData() != null
                            && responseDataResult.getData().getData() != null
                            && responseDataResult.getData().getData().getT() != null
                            && !CollectionUtils.isEmpty(responseDataResult.getData().getData().getT().getDataDetails())) {
                        callRealTimeList.addAll(responseDataResult.getData().getData().getT().getDataDetails());
                    }

                    //查找匹配的响应数据
                    CallRealTimeDTO matchedData = callRealTimeList.stream()
                            .filter(dto -> serialNo.equals(dto.getSerialNo()))
                            .findFirst()
                            .orElse(null);

                    if (matchedData != null) {
                        //匹配到数据，更新字段
                        originalData.setExtend(JSON.toJSONString(matchedData));
                    }else {
                        originalData.setExtend(null);
                    }
                    originalData.setStatus(QiFuProcessStatusEnum.UNPROCESSED.getCode());
                    originalData.setSelectStatus(QiFuSelectStatusEnum.QUERY_SUCCESS.getCode());
                    originalData.setUpdateTime(new Date());
                    resultList.add(originalData);
                    totalSuccess++;
                }
            }
        }

        logger.warn("事件推送实时查询外呼信息完成，原始数据数量：{}，处理数量：{}，成功数量：{}",
                qifuUploadDataOriginalList.size(), totalProcessed, totalSuccess);

        return resultList;
    }
}
