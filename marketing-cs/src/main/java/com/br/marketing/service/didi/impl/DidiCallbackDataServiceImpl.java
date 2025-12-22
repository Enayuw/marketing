package com.br.marketing.service.didi.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.didi.DiDiV5Client;
import com.br.marketing.client.didi.input.DiDiSmsRequestTO;
import com.br.marketing.client.didi.utils.MD5Util;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.DiDiV5CollidingDataLog;
import com.br.marketing.entity.DidiCallBackData;
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
            String token = pushConfig.getString("token") != null ?
                    pushConfig.getString("token") : "9Hqeoi36CJfdA7n4";
            Double samplingCallRate = pushConfig.getDouble("samplingCallRate") != null ?
                    pushConfig.getDouble("samplingCallRate") : 0;
            Double samplingSmsRate = pushConfig.getDouble("samplingSmsRate") != null ?
                    pushConfig.getDouble("samplingSmsRate") : 0;

            // 推送拨打成功的数据
            processStageData(pushPool, mediaName, token, null, 1);
            // 推送短信成功的数据
            processStageData(pushPool, mediaName, token, null, 2);
            // 构造拨打成功的数据
            processStageData(pushPool, mediaName, token, samplingCallRate, 3);
            // 构造短信成功的数据
            processStageData(pushPool, mediaName, token, samplingSmsRate, 4);
            // 处理触达失败数据
            processFailedData(pushPool, mediaName, token);
        } catch (Exception e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "触达回推job执行异常", TITLE), e);
        } finally {
            pushPool.shutdownAndAwaitTermination();
        }
    }

    /**
     * 分阶段处理数据
     */
    private void processStageData(TpDynamicExecutor pushPool, String mediaName, String token, Double samplingRate, int stage) {
        Long lastId = 0L;
        int pageSize = marketingCommonConfig.getDiDiV5Config().getInteger("limit");
        while (true) {
            if (marketingCommonConfig.getDiDiV5Config().getBooleanValue("interrupt")) {
                log.info("检测到中断信号，停止处理阶段{}的数据", stage);
                break;
            }

            List<DidiCallBackData> pageData = queryData(lastId, stage, pageSize);
            if (CollectionUtils.isEmpty(pageData)) {
                break;
            }

            Set<String> cellSet = pageData.stream().map(DidiCallBackData::getCell).collect(Collectors.toSet());
            List<String> pushedCells = didiCallBackDataLogMapper.selectPushedCells(cellSet);

            // 过滤已推送的cell
            List<DidiCallBackData> filteredData = pageData.stream()
                    .filter(data -> !pushedCells.contains(data.getCell()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(filteredData)) {
                lastId = pageData.get(pageData.size() - 1).getId();
                continue;
            }

            // 按cell分组，每个cell只取一条
            Map<String, List<DidiCallBackData>> cellGroupMap = filteredData.stream()
                    .collect(Collectors.groupingBy(DidiCallBackData::getCell));

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
                dataToPush = samplingData(uniqueData, samplingRate);
            }

            // 推送数据
            pushStageData(pushPool, dataToPush, mediaName, token, stage);
            lastId = pageData.get(pageData.size() - 1).getId();
        }
    }

    /**
     * 游标分页查询数据
     */
    private List<DidiCallBackData> queryData(Long lastId, int stage, int pageSize) {
        return switch (stage) {
            case 1 -> didiCallBackDataMapper.queryDidiCellSuccessData(pageSize, lastId);
            case 2 -> didiCallBackDataMapper.queryDidiSmsSuccessData(pageSize, lastId);
            case 3 -> didiCallBackDataMapper.queryDidiCellConstructData(pageSize, lastId);
            case 4 -> didiCallBackDataMapper.queryDidiSmsConstructData(pageSize, lastId);
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
        didiCallBackDataMapper.updateStatusByIds(duplicateIds, 1, 2);
    }

    /**
     * 推送阶段数据
     */
    private void pushStageData(TpDynamicExecutor pushPool, List<DidiCallBackData> dataList,
                               String mediaName, String token, int stage) {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        dataList.forEach(data -> pushPool.execute(() -> pushSingleSuccessData(data, mediaName, token, stage)
        ));
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
            saveCallbackDataLog(data, httpcode, content, pushType, pushStatus);
        } catch (Exception e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "触达成功数据回推异常，cell:" + data.getCell() + " id:" + data.getId(), TITLE), e);
            updateCallbackDataPushStatus(data.getId(), 2);
            saveCallbackDataLog(data, "500", e.getMessage(), 0, 0);
        }
    }

    /**
     * 构建成功请求参数
     */
    private DiDiSmsRequestTO buildSuccessRequest(DidiCallBackData data, String token, String mediaName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String cell = data.getCell();

        // 从扩展字段获取scas
        String scas = "";
        if (StringUtils.isNotBlank(data.getExtend())) {
            JSONObject extendJson = JSONObject.parseObject(data.getExtend());
            scas = extendJson.getString("scas");
        }

        return new DiDiSmsRequestTO()
                .setSign(cell)
                .setTimestamp(timestamp)
                .setSignature(MD5Util.encode(cell + timestamp + token))
                .setScas(scas)
                .setChannelId(mediaName);
    }

    /**
     * 蓄水池抽样
     */
    private List<DidiCallBackData> samplingData(List<DidiCallBackData> dataList, Double samplingRate) {
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

        if (sampleSize >= dataList.size()) {
            return dataList;
        }

        List<DidiCallBackData> reservoir = new ArrayList<>(sampleSize);

        // 前k个元素直接放入蓄水池
        for (int i = 0; i < sampleSize; i++) {
            reservoir.add(dataList.get(i));
        }

        // 处理剩余元素
        for (int i = sampleSize; i < dataList.size(); i++) {
            int j = RandomUtils.nextInt(0, i + 1);
            if (j < sampleSize) {
                reservoir.set(j, dataList.get(i));
            }
        }
        return reservoir;
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
    private void saveCallbackDataLog(DidiCallBackData data, String httpcode,
                                     String content, int pushType, int pushStatus) {
        DidiCallbackDataLog logEntity = new DidiCallbackDataLog();
        logEntity.setCallbackId(data.getId());
        logEntity.setCell(data.getCell());
        logEntity.setHttpCode(httpcode);
        logEntity.setReturnContent(content);
        logEntity.setPushType(pushType);
        logEntity.setPushStatus(pushStatus);
        logEntity.setCreateTime(new Date());
        didiCallBackDataLogMapper.insertSelective(logEntity);
    }

    private void processFailedData(TpDynamicExecutor pushPool, String mediaName, String token) {
        Long lastId = 0L;
        int pageSize = marketingCommonConfig.getDiDiV5Config().getInteger("limit");

        while (true) {
            if (marketingCommonConfig.getDiDiV5Config().getBooleanValue("interrupt")) {
                log.info("检测到中断信号，停止处理触达失败数据");
                break;
            }

            // 分页查询触达失败数据
            List<DiDiV5CollidingDataLog> pageData = didiV5CollidingDataLogMapper.queryFailedData(lastId, pageSize);
            if (CollectionUtils.isEmpty(pageData)) {
                break;
            }
            // 过滤已成功推送的数据（push_type in (1,2,3)）
            Set<String> succeedCellSet = pageData.stream()
                    .map(DiDiV5CollidingDataLog::getCell)
                    .collect(Collectors.toSet());
            List<String> successPushedCells = didiCallBackDataLogMapper.selectSuccessPushedCells(succeedCellSet);

            List<DiDiV5CollidingDataLog> filteredData = pageData.stream()
                    .filter(data -> !successPushedCells.contains(data.getCell()))
                    .collect(Collectors.toList());

            // 过滤已推送的cell
            Set<String> cellSet = filteredData.stream().map(DiDiV5CollidingDataLog::getCell).collect(Collectors.toSet());
            List<String> pushedCells = didiCallBackDataLogMapper.selectPushedCells(cellSet);
            filteredData = filteredData.stream()
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
                uniqueData.forEach(data ->
                        pushPool.execute(() -> pushSingleFailedData(data, mediaName, token))
                );
            }
            lastId = pageData.get(pageData.size() - 1).getId();
        }
    }

    /**
     * 推送单条失败数据
     */
    private void pushSingleFailedData(DiDiV5CollidingDataLog data, String mediaName, String token) {
        try {
            DiDiSmsRequestTO requestTO = buildFailedRequest(data, token);
            Result<String> response = diDiV5Client.callbackFailed(mediaName, requestTO);

            String resData = response.getData();
            JSONObject resJson = JSONObject.parseObject(resData);
            String httpcode = resJson.getString("httpcode");
            String content = resJson.getString("content");

            boolean success = "200".equals(httpcode);
            saveFailedCallbackDataLog(data, httpcode, content, success);

        } catch (Exception e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "触达失败数据回推异常，cell:" + data.getCell() + " id:" + data.getId(), TITLE), e);

            saveFailedCallbackDataLog(data, "500", e.getMessage(), false);
        }
    }

    /**
     * 构建失败请求参数
     */
    private DiDiSmsRequestTO buildFailedRequest(DiDiV5CollidingDataLog data, String token) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String cell = data.getCell();

        return new DiDiSmsRequestTO()
                .setSign(cell)
                .setTimestamp(timestamp)
                .setSignature(MD5Util.encode(cell + timestamp + token));
    }

    /**
     * 保存失败数据回调日志
     */
    private void saveFailedCallbackDataLog(DiDiV5CollidingDataLog data, String httpcode,
                                           String content, boolean success) {
        DidiCallbackDataLog logEntity = new DidiCallbackDataLog();
        logEntity.setCallbackId(data.getId());
        logEntity.setCell(data.getCell());
        logEntity.setHttpCode(httpcode);
        logEntity.setReturnContent(content);
        logEntity.setPushType(0);
        logEntity.setPushStatus(success ? 1 : 0);
        logEntity.setCreateTime(new Date());
        didiCallBackDataLogMapper.insertSelective(logEntity);
    }
}
