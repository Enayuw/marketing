package com.br.marketing.service.didi.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.client.didi.DiDiV5Client;
import com.br.marketing.client.didi.input.v5.DiDiV5CollidingRequestDTO;
import com.br.marketing.client.didi.output.v5.DiDiV5CollidingResultResponseDTO;
import com.br.marketing.client.didi.utils.MD5Util;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.DiDiCollidingDataRob;
import com.br.marketing.entity.DiDiDataLoopCycle;
import com.br.marketing.entity.DiDiV5CollidingDataLog;
import com.br.marketing.mapper.DiDiV5CollidingDataRobMapper;
import com.br.marketing.mapper.DiDiV5DataLoopCycleMapper;
import com.br.marketing.service.didi.DiDiCollidingDataNewService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DiDiCollidingDataNewServiceImpl implements DiDiCollidingDataNewService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DiDiV5DataLoopCycleMapper diDiV5DataLoopCycleMapper;

    @Resource
    private DiDiV5CollidingDataRobMapper diDiV5CollidingDataRobMapper;

    @Resource
    private DiDiV5Client diDiV5Client;

    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Override
    public void colliding(JobExecutionMultipleShardingContext context) {
        // 获取分片信息
        List<Integer> shardingItems = context.getShardingItems();
        int shardingTotalCount = context.getShardingTotalCount();
        log.warn("滴滴短信流量数据撞库任务开始执行，总分片数：{}，当前分片：{}", shardingTotalCount, shardingItems);

        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.DIDI_V5_COLLIDING.getName(), 50, 50);
        JSONObject collidingConfig = marketingCommonConfig.getDiDiV5Config();
        RateLimiter rateLimiter = RateLimiter.create(collidingConfig.getInteger("rateLimit"));

        String mediaName = collidingConfig.getString("mediaName") != null ? collidingConfig.getString("mediaName") : "bairongC";
        String token = collidingConfig.getString("token") != null ? collidingConfig.getString("token") : "9Hqeoi36CJfdA7n4";
        int leftLimit = collidingConfig.getInteger("collidingLimit") != null ? collidingConfig.getInteger("collidingLimit") : 3000000;
        List<String> retryHttpCode = collidingConfig.getJSONArray("retryHttpCode").toJavaList(String.class);

        // 收集所有异步任务的Future，用于等待所有任务完成
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        while (true) {
            JSONObject collidingConfig2 = marketingCommonConfig.getDiDiV5Config();
            boolean collidingSwitch = collidingConfig2.getBoolean("collidingSwitch") != null ? collidingConfig2.getBoolean("collidingSwitch") : false;
            if (collidingSwitch) {
                break;
            }

            String startTimeStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)) + " " + collidingConfig2.getString("firstBatchStartTime");
            String endTimeStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)) + " " + collidingConfig2.getString("firstBatchEndTime");
            DateTime startTime = DateUtil.parse(startTimeStr);
            DateTime endTime = DateUtil.parse(endTimeStr);

            if (DateUtil.compare(new Date(), endTime) >= 0 || DateUtil.compare(new Date(), startTime) < 0) {
                break;
            }
            if (leftLimit <= 0) {
                break;
            }

            int limit = collidingConfig2.getInteger("limit") != null ? collidingConfig2.getInteger("limit") : 2000;
            int actualLimit = Math.min(leftLimit, limit);

            List<DiDiDataLoopCycle> dataList = diDiV5DataLoopCycleMapper.queryCollidingDataBySharding(
                    actualLimit, DateUtil.beginOfDay(new Date()), new Date(), shardingTotalCount, shardingItems);

            if (CollectionUtils.isEmpty(dataList)) {
                break;
            }
            leftLimit -= dataList.size();

            markAsPushing(dataList);
            dataList.forEach(data -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(
                        () -> collidingData(data, mediaName, token, rateLimiter, retryHttpCode),
                        pushPool
                );
                futures.add(future);
            });
            if (leftLimit <= 0) {
                break;
            }
        }
        log.warn("分片{}等待所有撞库任务完成，共{}个任务", shardingItems, futures.size());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 处理非周期锁定的数据
        processRobData(leftLimit, shardingTotalCount, shardingItems, mediaName, token, rateLimiter,
                retryHttpCode, pushPool, futures, 2);
        processRobData(leftLimit, shardingTotalCount, shardingItems, mediaName, token, rateLimiter,
                retryHttpCode, pushPool, futures, 3);

        pushPool.shutdownAndAwaitTermination();
    }

    private void processRobData(int leftLimit, int shardingTotalCount,
                                List<Integer> shardingItems, String mediaName, String token, RateLimiter rateLimiter,
                                List<String> retryHttpCode, TpDynamicExecutor pushPool, List<CompletableFuture<Void>> futures,
                                int priority) {
        // 处理非周期锁定的数据
        List<CompletableFuture<Void>> futures3 = new ArrayList<>();
        while (true) {
            JSONObject collidingConfig2 = marketingCommonConfig.getDiDiV5Config();
            boolean collidingSwitch = collidingConfig2.getBoolean("collidingSwitch") != null ? collidingConfig2.getBoolean("collidingSwitch") : false;
            if (collidingSwitch) {
                break;
            }
            String startTimeStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)) + " " + collidingConfig2.getString("firstBatchStartTime");
            String endTimeStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)) + " " + collidingConfig2.getString("firstBatchEndTime");
            DateTime startTime = DateUtil.parse(startTimeStr);
            DateTime endTime = DateUtil.parse(endTimeStr);

            if (DateUtil.compare(new Date(), endTime) >= 0 || DateUtil.compare(new Date(), startTime) < 0) {
                break;
            }
            if (leftLimit <= 0) {
                break;
            }
            int limit = collidingConfig2.getInteger("limit") != null ? collidingConfig2.getInteger("limit") : 2000;
            int actualLimit = Math.min(leftLimit, limit);
            List<DiDiCollidingDataRob> dataList;
            if (priority == 2) {
                dataList = diDiV5CollidingDataRobMapper.queryCollidingDataBySharding(
                        actualLimit, DateUtil.beginOfDay(new Date()), new Date(), shardingTotalCount, shardingItems);
            } else {
                dataList = diDiV5CollidingDataRobMapper.queryUploadedData(
                        actualLimit, DateUtil.beginOfDay(new Date()), new Date(), shardingTotalCount, shardingItems);
            }

            if (CollectionUtils.isEmpty(dataList)) {
                break;
            }
            leftLimit -= dataList.size();

            markRobAsPushing(dataList);
            dataList.forEach(data -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(
                        () -> collidingData(data, mediaName, token, rateLimiter, retryHttpCode),
                        pushPool
                );
                futures3.add(future);
            });
            if (leftLimit <= 0) {
                break;
            }
        }
        log.warn("分片{}等待所有撞库任务完成，共{}个任务", shardingItems, futures.size());
        CompletableFuture.allOf(futures3.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 标记为正在处理状态
     *
     * @param dataList 数据列表
     * @author senyang.zheng
     * @since 2025/12/19
     */
    private void markAsPushing(List<DiDiDataLoopCycle> dataList) {
        List<Long> ids = dataList.stream().map(DiDiDataLoopCycle::getId).toList();
        diDiV5DataLoopCycleMapper.updatePushTimeByIds(new Date(), ids);
    }

    private void markRobAsPushing(List<DiDiCollidingDataRob> dataList) {
        List<Long> ids = dataList.stream().map(DiDiCollidingDataRob::getId).toList();
        diDiV5CollidingDataRobMapper.updatePushTimeByIds(new Date(), ids);
    }


    private void collidingData(DiDiDataLoopCycle data, String mediaName, String token, RateLimiter rateLimiter, List<String> retryHttpCode) {
        // 单个撞库异常不影响其他撞库
        try {
            // 尝试获取令牌
            boolean acquired = false;
            int retryCount = 0;
            final int maxRetries = 3;
            final long retryIntervalMs = 100;

            while (!acquired && retryCount < maxRetries) {
                acquired = rateLimiter.tryAcquire(1, 500, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        log.debug("请求被限流，进行第{}/{}次重试，手机号: {}", retryCount, maxRetries, data.getCell());
                        try {
                            Thread.sleep(retryIntervalMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            if (!acquired) {
                log.warn("手机号: {} 的撞库请求在重试{}次后仍被限流，将跳过处理", data.getCell(), maxRetries);
                return;
            }
            Result<String> response = diDiV5Client.colliding(mediaName, buildRequest(data.getCell(), token));
            String resData = response.getData();
            JSONObject resJson = JSONObject.parseObject(resData);
            String httpcode = resJson.getString("httpcode");
            String content = resJson.getString("content");
            boolean retry = retryHttpCode.contains(httpcode);
            if(retry) {
                return;
            }
            pushCycleDataToMq(data, httpcode, content);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "该手机号撞库异常：" + data.getCell() + "id:" + data.getId()), e);
        }
    }

    private void collidingData(DiDiCollidingDataRob data, String mediaName, String token, RateLimiter rateLimiter, List<String> retryHttpCode) {
        // 单个撞库异常不影响其他撞库
        try {
            // 尝试获取令牌
            boolean acquired = false;
            int retryCount = 0;
            final int maxRetries = 3;
            final long retryIntervalMs = 100;

            while (!acquired && retryCount < maxRetries) {
                acquired = rateLimiter.tryAcquire(1, 500, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        log.debug("请求被限流，进行第{}/{}次重试，手机号: {}", retryCount, maxRetries, data.getCell());
                        try {
                            Thread.sleep(retryIntervalMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            if (!acquired) {
                log.warn("手机号: {} 的撞库请求在重试{}次后仍被限流，将跳过处理", data.getCell(), maxRetries);
                return;
            }
            Result<String> response = diDiV5Client.colliding(mediaName, buildRequest(data.getCell(), token));
            String resData = response.getData();
            JSONObject resJson = JSONObject.parseObject(resData);
            String httpcode = resJson.getString("httpcode");
            String content = resJson.getString("content");
            boolean retry = retryHttpCode.contains(httpcode);
            if (retry) {
                return;
            }
            pushRobDataToMq(data, httpcode, content);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "该手机号撞库异常：" + data.getCell() + "id:" + data.getId()), e);
        }
    }

    private void pushRobDataToMq(DiDiCollidingDataRob data, String httpcode, String content) throws ParseException {
        log.warn("滴滴V5推送撞库日志消息content:{}", content);
        JSONObject mqJson = new JSONObject();
        DiDiV5CollidingDataLog diDiV5CollidingDataLog = new DiDiV5CollidingDataLog();
        diDiV5CollidingDataLog.setApiCode(data.getApiCode());
        diDiV5CollidingDataLog.setCell(data.getCell());
        diDiV5CollidingDataLog.setHttpCode(httpcode);
        diDiV5CollidingDataLog.setDataId(data.getId());
        diDiV5CollidingDataLog.setLocalId(data.getPackageId());
        diDiV5CollidingDataLog.setReturnContent(content);
        if (StringUtils.isNotBlank(content)) {
            DiDiV5CollidingResultResponseDTO diDiV5CollidingResultResponseDTO = JSON.parseObject(content,
                    DiDiV5CollidingResultResponseDTO.class);
            diDiV5CollidingDataLog.setErrorCode(diDiV5CollidingResultResponseDTO.getErrorCode());
            diDiV5CollidingDataLog.setErrorMessage(diDiV5CollidingResultResponseDTO.getErrorMessage());
            diDiV5CollidingDataLog.setResult(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getResult()));
            diDiV5CollidingDataLog.setFailReason(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getFailReason()));
            diDiV5CollidingDataLog.setUserGroup(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getUserGroup()));
            diDiV5CollidingDataLog.setNextTime(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getNextTime()));
            mqJson.put("diDiV5CollidingResultResponseDTO", diDiV5CollidingResultResponseDTO);

            data.setUpdateTime(new Date());
            if ("false".equalsIgnoreCase(diDiV5CollidingDataLog.getResult())) {
                if ("1".equalsIgnoreCase(diDiV5CollidingDataLog.getFailReason())) {
                    DiDiDataLoopCycle diDiDataLoopCycle = new DiDiDataLoopCycle();
                    BeanUtils.copyProperties(data, diDiDataLoopCycle);
                    diDiDataLoopCycle.setSourceType("F");
                    diDiDataLoopCycle.setLockType(2);
                    diDiDataLoopCycle.setCollidingTime(DateUtils.parse(diDiV5CollidingDataLog.getNextTime(), DateUtils.yyyyMMddHHmmss));
                    diDiV5DataLoopCycleMapper.insert(diDiDataLoopCycle);
                }
            }
            data.setPushTime(new Date());
            diDiV5CollidingDataRobMapper.updateByPrimaryKey(data);
        }
        mqJson.put("diDiV5CollidingDataLog", diDiV5CollidingDataLog);
        log.warn("滴滴V5推送撞库日志消息体:{}", mqJson.toJSONString());
        rocketMqSwitch.syncSend(MarketingOutsideInterfaceConstants.TOPIC, MarketingOutsideInterfaceConstants.TAG_MARKETING_DIDI_V5_COLLIDING_DATA,
                mqJson.toJSONString());
    }

    private void pushCycleDataToMq(DiDiDataLoopCycle data, String httpcode, String content) throws ParseException {
        log.warn("滴滴V5推送撞库日志消息content:{}", content);
        JSONObject mqJson = new JSONObject();
        DiDiV5CollidingDataLog diDiV5CollidingDataLog = new DiDiV5CollidingDataLog();
        diDiV5CollidingDataLog.setApiCode(data.getApiCode());
        diDiV5CollidingDataLog.setCell(data.getCell());
        diDiV5CollidingDataLog.setHttpCode(httpcode);
        diDiV5CollidingDataLog.setDataId(data.getId());
        diDiV5CollidingDataLog.setLocalId(Long.getLong(data.getPackageId()));
        diDiV5CollidingDataLog.setReturnContent(content);
        if (StringUtils.isNotBlank(content)) {
            DiDiV5CollidingResultResponseDTO diDiV5CollidingResultResponseDTO = JSON.parseObject(content,
                    DiDiV5CollidingResultResponseDTO.class);
            diDiV5CollidingDataLog.setErrorCode(diDiV5CollidingResultResponseDTO.getErrorCode());
            diDiV5CollidingDataLog.setErrorMessage(diDiV5CollidingResultResponseDTO.getErrorMessage());
            diDiV5CollidingDataLog.setResult(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getResult()));
            diDiV5CollidingDataLog.setFailReason(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getFailReason()));
            diDiV5CollidingDataLog.setUserGroup(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getUserGroup()));
            diDiV5CollidingDataLog.setNextTime(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getNextTime()));
            mqJson.put("diDiV5CollidingResultResponseDTO", diDiV5CollidingResultResponseDTO);

            data.setUpdateTime(new Date());
            data.setPushTime(new Date());
            if ("false".equalsIgnoreCase(diDiV5CollidingDataLog.getResult())) {
                if ("1".equalsIgnoreCase(diDiV5CollidingDataLog.getFailReason())) {
                    data.setLockType(2);
                    data.setCollidingTime(DateUtils.parse(diDiV5CollidingDataLog.getNextTime(), DateUtils.yyyyMMddHHmmss));
                }
            }
            diDiV5DataLoopCycleMapper.updateByPrimaryKey(data);
        }
        mqJson.put("diDiV5CollidingDataLog", diDiV5CollidingDataLog);
        log.warn("滴滴V5推送撞库日志消息体:{}", mqJson.toJSONString());
        rocketMqSwitch.syncSend(MarketingOutsideInterfaceConstants.TOPIC, MarketingOutsideInterfaceConstants.TAG_MARKETING_DIDI_V5_COLLIDING_DATA,
                mqJson.toJSONString());
    }

    private DiDiV5CollidingRequestDTO buildRequest(String cell, String token) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return new DiDiV5CollidingRequestDTO().setSign(cell).setTimestamp(timestamp).setSignature(MD5Util.encode(cell + timestamp + token));
    }
}