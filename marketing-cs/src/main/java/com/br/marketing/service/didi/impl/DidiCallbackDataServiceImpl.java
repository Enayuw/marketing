package com.br.marketing.service.didi.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.didi.DiDiV5Client;
import com.br.marketing.client.didi.input.DiDiSmsRequestTO;
import com.br.marketing.client.didi.utils.MD5Util;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.DiDiV5CollidingDataLog;
import com.br.marketing.entity.DidiCallBackData;
import com.br.marketing.entity.DidiCallBackDataExample;
import com.br.marketing.entity.DidiCallbackDataLog;
import com.br.marketing.mapper.DiDiV5CollidingDataLogMapper;
import com.br.marketing.mapper.DidiCallBackDataMapper;
import com.br.marketing.mapper.DidiCallbackDataLogMapper;
import com.br.marketing.service.didi.DidiCallbackDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DidiCallbackDataServiceImpl implements DidiCallbackDataService {

    private final static String TITLE = "【滴滴V5-触达回推数据】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DidiCallBackDataMapper didiCallBackDataMapper;

    @Resource
    private DidiCallbackDataLogMapper didiCallBackDataLogMapper;

    @Resource
    private DiDiV5CollidingDataLogMapper didiV5CollidingDataLogMapper;

    @Resource
    private DiDiV5Client diDiV5Client;

    /**
     * 触达回推job执行方法
     */
    @Override
    public void process() {
        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.DIDI_V5_CALLBACK.getName(), 50, 50);

        try {
            JSONObject pushConfig = marketingCommonConfig.getDiDiV5Config();
            String mediaName = pushConfig.getString("mediaName") != null ?
                    pushConfig.getString("mediaName") : "bairongC";
            String successToken = pushConfig.getString("successToken") != null ?
                    pushConfig.getString("successToken") : "DK&SgWl!fZ%WVSXe";
            String failToken = pushConfig.getString("failToken") != null ?
                    pushConfig.getString("failToken") : "ZRR%Z1iQtppsHYay";
            Double samplingCallRate = pushConfig.getDouble("samplingCallRate") != null ?
                    pushConfig.getDouble("samplingCallRate") : 0;
            Double samplingSmsRate = pushConfig.getDouble("samplingSmsRate") != null ?
                    pushConfig.getDouble("samplingSmsRate") : 0;
            String apiCode = pushConfig.getString("apiCode");

            // 推送拨打成功的数据
            processStageData(pushPool, mediaName, successToken, null, 1, apiCode);
            // 推送短信成功的数据
            processStageData(pushPool, mediaName, successToken, null, 2, apiCode);
            // 构造拨打成功的数据
            processStageData(pushPool, mediaName, successToken, samplingCallRate, 3, apiCode);
            // 构造短信成功的数据
            processStageData(pushPool, mediaName, successToken, samplingSmsRate, 4, apiCode);
            // 处理触达失败数据
            processFailedData(pushPool, mediaName, failToken, apiCode);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "触达回推job执行异常", TITLE), e);
        } finally {
            pushPool.shutdownAndAwaitTermination();
        }
    }

    /**
     * 分阶段处理数据
     */
    private void processStageData(TpDynamicExecutor pushPool, String mediaName, String token, Double samplingRate, int stage,
                                  String apiCode) {
        Long lastId = 0L;
        int pageSize = marketingCommonConfig.getDiDiV5Config().getInteger("limit");
        while (true) {
            if (marketingCommonConfig.getDiDiV5Config().getBooleanValue("callbackSwitch")) {
                log.warn("检测到中断信号，停止处理阶段{}的数据", stage);
                break;
            }

            List<DidiCallBackData> pageData = queryData(lastId, stage, pageSize, apiCode);
            if (CollectionUtils.isEmpty(pageData)) {
                break;
            }

            Set<String> cellSet = pageData.stream().map(DidiCallBackData::getCustNum).collect(Collectors.toSet());
            List<String> pushedCells = didiCallBackDataLogMapper.selectPushedCells(cellSet);
            if(CollectionUtils.isNotEmpty(pushedCells)) {
                List<Long> duplicateIds = pageData.stream()
                        .filter(data -> pushedCells.contains(data.getCustNum()))
                        .map(DidiCallBackData::getId)
                        .collect(Collectors.toList());
                didiCallBackDataMapper.updateStatusByIds(duplicateIds, 1);
            }

            // 过滤已推送的cell
            List<DidiCallBackData> filteredData = pageData.stream()
                    .filter(data -> !pushedCells.contains(data.getCustNum()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(filteredData)) {
                lastId = pageData.get(pageData.size() - 1).getId();
                continue;
            }

            // 按cell分组，每个cell只取一条
            Map<String, List<DidiCallBackData>> cellGroupMap = filteredData.stream()
                    .collect(Collectors.groupingBy(DidiCallBackData::getCustNum));

            List<DidiCallBackData> uniqueData = new ArrayList<>();
            for (List<DidiCallBackData> cellDataList : cellGroupMap.values()) {
                DidiCallBackData selectedData = cellDataList.get(0);
                uniqueData.add(selectedData);

                // 标记同cell的其他数据为不推送
                if (cellDataList.size() > 1) {
                    markDuplicateCellsAsNotPush(cellDataList, selectedData.getId());
                }
            }

            List<DidiCallBackData> dataToPush;
            if (stage == 1 || stage == 2) {
                dataToPush = uniqueData;
            } else {
                // 抽样并构造拨打/短信数据
                dataToPush = samplingData(uniqueData, samplingRate, stage);
            }

            // 推送数据
            pushStageData(pushPool, dataToPush, mediaName, token, stage);
            lastId = pageData.get(pageData.size() - 1).getId();
        }
    }

    /**
     * 游标分页查询数据
     */
    private List<DidiCallBackData> queryData(Long lastId, int stage, int pageSize, String  apiCode) {
        return switch (stage) {
            case 1 -> didiCallBackDataMapper.queryDidiCellSuccessData(pageSize, lastId, apiCode);
            case 2 -> didiCallBackDataMapper.queryDidiSmsSuccessData(pageSize, lastId, apiCode);
            case 3 -> didiCallBackDataMapper.queryDidiCellConstructData(pageSize, lastId, apiCode);
            case 4 -> didiCallBackDataMapper.queryDidiSmsConstructData(pageSize, lastId, apiCode);
            default -> Lists.newArrayList();
        };
    }

    /**
     * 标记重复cell的数据状态
     */
    private void markDuplicateCellsAsNotPush(List<DidiCallBackData> cellDataList, Long selectedId) {
        List<Long> duplicateIds = cellDataList.stream()
                .map(DidiCallBackData::getId)
                .filter(id -> !id.equals(selectedId))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(duplicateIds)) {
            return;
        }
        didiCallBackDataMapper.updateStatusByIds(duplicateIds, 1);
    }

    /**
     * 推送阶段数据
     */
    private void pushStageData(TpDynamicExecutor pushPool, List<DidiCallBackData> dataList,
                               String mediaName, String token, int stage) {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        dataList.forEach(data -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                    pushSingleSuccessData(data, mediaName, token, stage), pushPool);
            futures.add(future);
        });
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 推送单条成功数据
     */
    private void pushSingleSuccessData(DidiCallBackData data, String mediaName,
                                       String token, int stage) {
        try {
            DiDiSmsRequestTO requestTO = buildSuccessRequest(data, token, mediaName);
            Result<String> response = diDiV5Client.callbackSuccess(mediaName, requestTO);

            String resData = response.getData();
            JSONObject resJson = JSONObject.parseObject(resData);
            String httpcode = resJson.getString("httpcode");
            String content = resJson.getString("content");

            boolean success = "200".equals(httpcode);
            int pushStatus = success ? 1 : 2;
            // 更新推送状态
            updateCallbackDataPushStatus(data.getId(), pushStatus);
            int pushType;
            if (stage == 3) {
                pushType = 2; // 构造拨打成功
            } else if (stage == 4) {
                pushType = 3; // 构造短信成功
            } else {
                pushType = 1;
            }
            JSONObject contentJson = JSONObject.parseObject(content);
            saveCallbackDataLog(data, httpcode, content, contentJson.getString("errorCode"), contentJson.getString("errorMessage"),
                    pushType, pushStatus, requestTO);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "触达成功数据回推异常，cell:" + data.getCell() + " id:" + data.getId(), TITLE), e);
            updateCallbackDataPushStatus(data.getId(), 2);
            saveCallbackDataLog(data, "500", e.getMessage(), null, null, null, 0, null);
        }
    }

    /**
     * 构建成功请求参数
     */
    private DiDiSmsRequestTO buildSuccessRequest(DidiCallBackData data, String token, String mediaName) {
        String timestamp;
        if (1 == data.getCallbackType()) {
            JSONObject dataJson = JSONObject.parseObject(data.getExtend());
            timestamp = dataJson.getString("callStartTime");
        } else {
            timestamp = String.valueOf(data.getCreateTime().getTime());
        }
        String custNum = data.getCustNum();
        return new DiDiSmsRequestTO().setSign(custNum)
                .setMediaName(mediaName).setChannelId("3140744898058385-bairongC")
                .setTimestamp(timestamp)
                .setSignature(MD5Util.encode(custNum + timestamp + token))
                .setScas(data.getScas());
    }

    /**
     * 蓄水池抽样
     */
    private List<DidiCallBackData> samplingData(List<DidiCallBackData> dataList, Double samplingRate, int stage) {
        if (CollectionUtils.isEmpty(dataList) || samplingRate >= 1.0) {
            return dataList;
        }
        if (samplingRate == 0) {
            return Lists.newArrayList();
        }

        int sampleSize = new BigDecimal(dataList.size())
                .multiply(BigDecimal.valueOf(samplingRate))
                .setScale(0, RoundingMode.UP)
                .intValue();
        List<DidiCallBackData> reservoirs = new ArrayList<>(sampleSize);
        // 前k个元素直接放入蓄水池
        for (int i = 0; i < sampleSize; i++) {
            DidiCallBackData sample = prepareSample(dataList.get(i), stage);
            reservoirs.add(sample);
        }
        // 处理剩余元素
        for (int i = sampleSize; i < dataList.size(); i++) {
            // 生成[0, i]的随机数
            int j = RandomUtils.nextInt(0, i + 1);
            if (j < sampleSize) {
                DidiCallBackData sample = prepareSample(dataList.get(i), stage);
                reservoirs.set(j, sample);
            }
        }
        return reservoirs;
    }

    private DidiCallBackData prepareSample(DidiCallBackData data, int stage) {
        DidiCallBackData sample = new DidiCallBackData();
        sample.setId(data.getId());
        sample.setCustNum(data.getCustNum());
        sample.setCell(data.getCell());
        sample.setScas(data.getScas());
        sample.setApiCode(data.getApiCode());
        sample.setCreateTime(data.getCreateTime());
        sample.setExtend(data.getExtend());

        // 根据阶段设置特定字段
        if (stage == 3) {
            sample.setIsConnect(1);
            sample.setCallbackType(1);
        } else {
            sample.setSmsSendStatus(1);
            sample.setCallbackType(2);
        }
        return sample;
    }

    /**
     * 更新回调数据推送状态
     */
    private void updateCallbackDataPushStatus(Long id, int pushStatus) {
        DidiCallBackData updateData = new DidiCallBackData();
        updateData.setId(id);
        updateData.setPushStatus(pushStatus);
        updateData.setUpdateTime(new Date());
        didiCallBackDataMapper.updateByPrimaryKeySelective(updateData);
    }

    /**
     * 保存回调数据日志
     */
    private void saveCallbackDataLog(DidiCallBackData data, String httpcode, String errorCode, String errorMessage,
                                     String content, Integer pushType, int pushStatus, DiDiSmsRequestTO requestTO) {
        DidiCallbackDataLog logEntity = new DidiCallbackDataLog();
        logEntity.setCallbackId(data.getId());
        logEntity.setCell(data.getCustNum());
        logEntity.setHttpCode(httpcode);
        logEntity.setReturnContent(content);
        logEntity.setPushType(pushType);
        logEntity.setPushStatus(pushStatus);
        logEntity.setApiCode(data.getApiCode());
        logEntity.setCreateTime(new Date());
        logEntity.setScas(data.getScas());
        logEntity.setErrorCode(errorCode);
        logEntity.setErrorMessage(errorMessage);
        if(Objects.nonNull(requestTO)) {
            logEntity.setSignature(requestTO.getSignature());
            logEntity.setTimestamp(requestTO.getTimestamp());
            logEntity.setChannelId(requestTO.getChannelId());
            logEntity.setMeidaName(requestTO.getMediaName());
        }
        didiCallBackDataLogMapper.insertSelective(logEntity);
    }

    private void processFailedData(TpDynamicExecutor pushPool, String mediaName, String token, String apiCode) {
        Long lastId = 0L;
        int pageSize = marketingCommonConfig.getDiDiV5Config().getInteger("limit");

        while (true) {
            if (marketingCommonConfig.getDiDiV5Config().getBooleanValue("callbackFailSwitch")) {
                log.warn("检测到中断信号，停止处理触达失败数据");
                break;
            }

            // 分页查询触达失败数据
            List<DiDiV5CollidingDataLog> pageData = didiV5CollidingDataLogMapper.queryFailedData(lastId, pageSize, apiCode);
            if (CollectionUtils.isEmpty(pageData)) {
                break;
            }
            // 过滤已推送的cell
            Set<String> cellSet = pageData.stream().map(DiDiV5CollidingDataLog::getCell).collect(Collectors.toSet());
            List<String> pushedCells = didiCallBackDataLogMapper.selectPushedCells(cellSet);
            if(CollectionUtils.isNotEmpty(pushedCells)) {
                didiCallBackDataMapper.updateStatusByCells(1, pushedCells);
            }
            List<DiDiV5CollidingDataLog> filteredData = pageData.stream()
                    .filter(data -> !pushedCells.contains(data.getCell()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(filteredData)) {
                lastId = pageData.get(pageData.size() - 1).getId();
                continue;
            }
            // 按cell分组，每个cell只取一条
            Map<String, List<DiDiV5CollidingDataLog>> cellGroupMap = filteredData.stream()
                    .collect(Collectors.groupingBy(DiDiV5CollidingDataLog::getCell));

            List<DiDiV5CollidingDataLog> uniqueData = new ArrayList<>();
            for (List<DiDiV5CollidingDataLog> cellDataList : cellGroupMap.values()) {
                DiDiV5CollidingDataLog selectedData = cellDataList.get(0);
                uniqueData.add(selectedData);
            }
            if (!CollectionUtils.isEmpty(uniqueData)) {
                // 推送失败数据
                List<CompletableFuture<Void>> futures = Lists.newArrayList();
                uniqueData.forEach(data -> {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                            pushSingleFailedData(data, mediaName, token), pushPool);
                    futures.add(future);
                });
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }
            // 将【didi_v5_callback_data】中的数据标记为已推送
            DidiCallBackDataExample example = new DidiCallBackDataExample();
            example.createCriteria().andCreateTimeGreaterThan(new Date())
                    .andCustNumIn(pageData.stream().map(DiDiV5CollidingDataLog::getCell).collect(Collectors.toList()));
            DidiCallBackData updateData = new DidiCallBackData();
            updateData.setPushStatus(1);
            didiCallBackDataMapper.updateByExampleSelective(updateData, example);

            lastId = pageData.get(pageData.size() - 1).getId();
        }
    }

    /**
     * 推送单条失败数据
     */
    private void pushSingleFailedData(DiDiV5CollidingDataLog data, String mediaName, String token) {
        try {
            DiDiSmsRequestTO requestTO = buildFailedRequest(data, token, mediaName);
            Result<String> response = diDiV5Client.callbackFailed(mediaName, requestTO);

            String resData = response.getData();
            JSONObject resJson = JSONObject.parseObject(resData);
            String httpcode = resJson.getString("httpcode");
            String content = resJson.getString("content");
            JSONObject contentJson = JSONObject.parseObject(content);
            String errorCode = contentJson.getString("errorCode");
            String errorMessage = contentJson.getString("errorMessage");

            boolean success = "200".equals(httpcode);
            saveFailedCallbackDataLog(data, httpcode, content, success, errorCode, errorMessage, requestTO);

        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "触达失败数据回推异常，cell:" + data.getCell() + " id:" + data.getId(), TITLE), e);

            saveFailedCallbackDataLog(data, "500", e.getMessage(), false, null, null, null);
        }
    }

    /**
     * 构建失败请求参数
     */
    private DiDiSmsRequestTO buildFailedRequest(DiDiV5CollidingDataLog data, String token, String mediaName) {
        String timestamp = generateRandomTimestampToday();
        String custNum = data.getCell();

        return new DiDiSmsRequestTO()
                .setSign(custNum).setMediaName(mediaName)
                .setTimestamp(timestamp)
                .setSignature(MD5Util.encode(custNum + timestamp + token));
    }

    private String generateRandomTimestampToday() {
        Random random = new Random();
        Date today = new Date();
        long todayStart = today.getTime() / (1000 * 60 * 60 * 24) * (1000 * 60 * 60 * 24);
        long startTime = todayStart + (14 * 60 * 60 * 1000);
        long endTime = todayStart + (18 * 60 * 60 * 1000);
        // 生成14:00-18:00之间的随机时间戳
        long randomTime = startTime + (long)(random.nextDouble() * (endTime - startTime));
        return String.valueOf(randomTime);
    }

    /**
     * 保存失败数据回调日志
     */
    private void saveFailedCallbackDataLog(DiDiV5CollidingDataLog data, String httpcode,
                                           String content, boolean success, String errorCode, String errorMessage,
                                           DiDiSmsRequestTO requestTO) {
        DidiCallbackDataLog logEntity = new DidiCallbackDataLog();
        logEntity.setCallbackId(data.getId());
        logEntity.setCell(data.getCell());
        logEntity.setHttpCode(httpcode);
        logEntity.setReturnContent(content);
        logEntity.setPushType(0);
        logEntity.setApiCode(data.getApiCode());
        logEntity.setPushStatus(success ? 1 : 0);
        logEntity.setCreateTime(new Date());
        logEntity.setErrorCode(errorCode);
        logEntity.setErrorMessage(errorMessage);
        if(Objects.nonNull(requestTO)) {
            logEntity.setSignature(requestTO.getSignature());
            logEntity.setTimestamp(requestTO.getTimestamp());
            logEntity.setMeidaName(requestTO.getMediaName());
        }
        didiCallBackDataLogMapper.insertSelective(logEntity);
    }
}
