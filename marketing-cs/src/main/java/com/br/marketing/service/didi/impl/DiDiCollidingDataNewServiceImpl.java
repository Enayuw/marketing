package com.br.marketing.service.didi.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.didi.DiDiV5Client;
import com.br.marketing.client.didi.input.v5.DiDiV5CollidingRequestDTO;
import com.br.marketing.client.didi.output.v5.DiDiV5CollidingResultResponseDTO;
import com.br.marketing.client.didi.utils.MD5Util;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.DiDiV5CollidingDataRobMapper;
import com.br.marketing.mapper.DiDiV5DataLoopCycleMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.didi.DiDiCollidingDataNewService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.api.client.util.Sets;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.curator.shaded.com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    @Resource
    private LocalFileMapper localFileMapper;

    @Override
    public void colliding(JobExecutionMultipleShardingContext context) {
        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.DIDI_V5_COLLIDING.getName(), 50, 50);
        JSONObject collidingConfig = marketingCommonConfig.getDiDiV5Config();
        RateLimiter rateLimiter = RateLimiter.create(collidingConfig.getInteger("rateLimit"));

        String startTimeStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)) + " "
                + collidingConfig.getString("firstBatchStartTime");
        String endTimeStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)) + " "
                + collidingConfig.getString("firstBatchEndTime");
        DateTime startTime = DateUtil.parse(startTimeStr);
        DateTime endTime = DateUtil.parse(endTimeStr);

        AtomicInteger leftLimit = new AtomicInteger();
        if (DateUtil.compare(new Date(), endTime) < 0 && DateUtil.compare(new Date(), startTime) >= 0) {
            leftLimit.set(collidingConfig.getInteger("collidingLimit"));
        } else {
            startTimeStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)) + " "
                    + collidingConfig.getString("secondBatchStartTime");
            endTimeStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)) + " "
                    + collidingConfig.getString("secondBatchEndTime");
            startTime = DateUtil.parse(startTimeStr);
            endTime = DateUtil.parse(endTimeStr);
            if (DateUtil.compare(new Date(), endTime) < 0 && DateUtil.compare(new Date(), startTime) >= 0) {
                leftLimit.set(collidingConfig.getInteger("collidingLimit2"));
            }
        }

        if (Objects.equals(0, leftLimit.get())) {
            return;
        }

        String mediaName = collidingConfig.getString("mediaName") != null ? collidingConfig.getString("mediaName") : "bairongC";
        String token = collidingConfig.getString("token") != null ? collidingConfig.getString("token") : "9Hqeoi36CJfdA7n4";

        DiDiDataLoopCycleExample example = new DiDiDataLoopCycleExample();
        example.createCriteria().andApiCodeEqualTo(collidingConfig.getString("apiCode"))
                .andPushTimeBetween(startTime, endTime).andIsDeleteEqualTo(0);
        int cycleCount = diDiV5DataLoopCycleMapper.countByExample(example);

        DiDiCollidingDataRobExample robExample = new DiDiCollidingDataRobExample();
        robExample.createCriteria().andApiCodeEqualTo(collidingConfig.getString("apiCode"))
                .andPushTimeBetween(startTime, endTime).andIsDeleteEqualTo(0);
        int robCount = diDiV5CollidingDataRobMapper.countByExample(robExample);

        int collidedCount = cycleCount + robCount;

        if(collidedCount >= leftLimit.get()) {
            log.warn("滴滴短信流量数据撞库任务停止执行，已超过限制");
            return;
        }
        leftLimit.addAndGet(-collidedCount);

        List<String> retryHttpCode = collidingConfig.getJSONArray("retryHttpCode").toJavaList(String.class);
        Set<Long> packageIds = Sets.newHashSet();
        LocalFileExample fileExample = new LocalFileExample();
        fileExample.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.DD.getValue())
                .andPushStartTimeIsNull();
        List<LocalFile> localFiles = localFileMapper.selectByExample(fileExample);
        if(!CollectionUtils.isEmpty(localFiles)) {
            List<Long> fileIds = localFiles.stream().map(LocalFile::getId).collect(Collectors.toList());
            localFileMapper.updateUploadStartTimeById(fileIds, new Date());
        }

        // 收集所有异步任务的Future，用于等待所有任务完成
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        while (true) {
            JSONObject collidingConfig2 = marketingCommonConfig.getDiDiV5Config();
            boolean collidingSwitch = collidingConfig2.getBoolean("collidingSwitch");
            Integer partition = collidingConfig.getInteger("partition");
            if (collidingSwitch) {
                break;
            }
            if (DateUtil.compare(new Date(), endTime) >= 0 || DateUtil.compare(new Date(), startTime) < 0) {
                break;
            }
            if (leftLimit.get() <= 0) {
                break;
            }

            int limit = collidingConfig2.getInteger("limit") != null ? collidingConfig2.getInteger("limit") : 2000;
            int actualLimit = Math.min(leftLimit.get(), limit);

            List<DiDiDataLoopCycle> dataList = diDiV5DataLoopCycleMapper.queryCollidingDataBySharding(
                    actualLimit, DateUtil.beginOfDay(new Date()), new Date());

            if (CollectionUtils.isEmpty(dataList)) {
                break;
            }
            packageIds.addAll(dataList.stream().map(DiDiDataLoopCycle::getPackageId).map(Long::parseLong).collect(Collectors.toSet()));
            leftLimit.addAndGet(-dataList.size());
            markAsPushing(dataList);
            List<List<DiDiDataLoopCycle>> lists = ListUtils.partition(dataList, partition);
            lists.forEach(list -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(
                        () -> {
                            for (DiDiDataLoopCycle data : list) {
                                collidingData(data, mediaName, token, rateLimiter, retryHttpCode);
                            }
                        },
                        pushPool
                );
                futures.add(future);
            });
            if (leftLimit.get() <= 0) {
                break;
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 处理非周期锁定的数据
        processRobData(leftLimit, mediaName, token, rateLimiter,
                retryHttpCode, pushPool, 2, packageIds, startTime, endTime);
        processRobData(leftLimit, mediaName, token, rateLimiter,
                retryHttpCode, pushPool, 3, packageIds, startTime, endTime);
        updateLocalFiles(packageIds);
        pushPool.shutdownAndAwaitTermination();
    }

    private void updateLocalFiles(Set<Long> fileIds) {
        for (Long fileId : fileIds) {
            DiDiDataLoopCycleExample example = new DiDiDataLoopCycleExample();
            example.createCriteria().andPackageIdEqualTo(fileId.toString()).andIsDeleteEqualTo(0)
                    .andPushTimeIsNotNull();
            int cycleCount = diDiV5DataLoopCycleMapper.countByExample(example);

            DiDiCollidingDataRobExample robExample = new DiDiCollidingDataRobExample();
            robExample.createCriteria().andPackageIdEqualTo(fileId).andIsDeleteEqualTo(0)
                    .andPushTimeIsNotNull();
            int robCount = diDiV5CollidingDataRobMapper.countByExample(robExample);

            localFileMapper.updatePushEndTimeById(fileId, cycleCount + robCount, new Date());
        }
    }

    private void processRobData(AtomicInteger leftLimit, String mediaName, String token, RateLimiter rateLimiter,
                                List<String> retryHttpCode, TpDynamicExecutor pushPool,
                                int priority, Set<Long> packageIds, Date startTime, Date endTime) {
        // 处理非周期锁定的数据
        List<CompletableFuture<Void>> futures3 = new ArrayList<>();
        while (true) {
            JSONObject collidingConfig = marketingCommonConfig.getDiDiV5Config();
            boolean collidingSwitch = collidingConfig.getBoolean("collidingSwitch");
            if (collidingSwitch) {
                break;
            }

            if (DateUtil.compare(new Date(), endTime) >= 0 || DateUtil.compare(new Date(), startTime) < 0) {
                break;
            }
            if (leftLimit.get() <= 0) {
                break;
            }
            Integer partition = collidingConfig.getInteger("partition");
            int limit = collidingConfig.getInteger("limit") != null ? collidingConfig.getInteger("limit") : 2000;
            int actualLimit = Math.min(leftLimit.get(), limit);
            List<DiDiCollidingDataRob> dataList;
            if (priority == 2) {
                dataList = diDiV5CollidingDataRobMapper.queryCollidingDataBySharding(
                        actualLimit, startTime, new Date());
            } else {
                dataList = diDiV5CollidingDataRobMapper.queryUploadedData(
                        actualLimit, DateUtil.beginOfDay(new Date()), new Date());
            }

            if (CollectionUtils.isEmpty(dataList)) {
                break;
            }
            packageIds.addAll(dataList.stream().map(DiDiCollidingDataRob::getPackageId).collect(Collectors.toSet()));

            leftLimit.addAndGet(-dataList.size());

            markRobAsPushing(dataList);
            List<List<DiDiCollidingDataRob>> lists = ListUtils.partition(dataList, partition);
            lists.forEach(list -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(
                        () -> {
                            for (DiDiCollidingDataRob data : list) {
                                collidingData(data, mediaName, token, rateLimiter, retryHttpCode);
                            }
                        },
                        pushPool
                );
                futures3.add(future);
            });
            if (leftLimit.get() <= 0) {
                break;
            }
        }
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
        try {
            diDiV5DataLoopCycleMapper.updatePushTimeByIds(new Date(), ids);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage()
                    , "滴滴V5数据送撞异常"), e);
            try {
                diDiV5DataLoopCycleMapper.updatePushTimeByIds(new Date(), ids);
            } catch (Exception ex) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), ex.getMessage()
                        , "滴滴V5数据送撞2次异常"), ex);
                try {
                    diDiV5DataLoopCycleMapper.updatePushTimeByIds(new Date(), ids);
                } catch (Exception exception) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), exception.getMessage()
                            , "滴滴V5数据送撞3次异常"), exception);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void markRobAsPushing(List<DiDiCollidingDataRob> dataList) {
        List<Long> ids = dataList.stream().map(DiDiCollidingDataRob::getId).toList();
        try {
            diDiV5CollidingDataRobMapper.updatePushTimeByIds(new Date(), ids);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage()
                    , "滴滴V5数据送撞异常"), e);
            try {
                diDiV5CollidingDataRobMapper.updatePushTimeByIds(new Date(), ids);
            } catch (Exception ex) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), ex.getMessage()
                        , "滴滴V5数据送撞2次异常"), ex);
                try {
                    diDiV5CollidingDataRobMapper.updatePushTimeByIds(new Date(), ids);
                } catch (Exception exception) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), exception.getMessage()
                            , "滴滴V5数据送撞3次异常"), exception);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


    private void collidingData(DiDiDataLoopCycle data, String mediaName, String token, RateLimiter rateLimiter, List<String> retryHttpCode) {
        // 单个撞库异常不影响其他撞库
        try {
            // 尝试获取令牌
            boolean acquired = false;
            int retryCount = 0;
            long retryIntervalMs = 100;
            int maxRetries = 3;
            while (!acquired && retryCount < maxRetries) {
                acquired = rateLimiter.tryAcquire(1, 500, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        log.warn("请求被限流，进行第{}/{}次重试，手机号: {}", retryCount, maxRetries, data.getCell());
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
                data.setPushTime(null);
                diDiV5DataLoopCycleMapper.updateByPrimaryKey(data);
                return;
            }
            Result<String> response = diDiV5Client.colliding(mediaName, buildRequest(data.getCell(), token));
            String resData = response.getData();
            JSONObject resJson = JSONObject.parseObject(resData);
            String httpcode = resJson.getString("httpcode");
            String content = resJson.getString("content");
            boolean retry = retryHttpCode.contains(httpcode);
            if(retry) {
                data.setPushTime(null);
                diDiV5DataLoopCycleMapper.updateByPrimaryKey(data);
                return;
            }
            pushCycleDataToMq(data, httpcode, content);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
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
            int maxRetries = 3;
            long retryIntervalMs = 100;

            while (!acquired && retryCount < maxRetries) {
                acquired = rateLimiter.tryAcquire(1, 500, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        log.warn("请求被限流，进行第{}/{}次重试，手机号: {}", retryCount, maxRetries, data.getCell());
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
                data.setPushTime(null);
                diDiV5CollidingDataRobMapper.updateByPrimaryKey(data);
                return;
            }
            Result<String> response = diDiV5Client.colliding(mediaName, buildRequest(data.getCell(), token));
            String resData = response.getData();
            JSONObject resJson = JSONObject.parseObject(resData);
            String httpcode = resJson.getString("httpcode");
            String content = resJson.getString("content");
            boolean retry = retryHttpCode.contains(httpcode);
            if (retry) {
                data.setPushTime(null);
                diDiV5CollidingDataRobMapper.updateByPrimaryKey(data);
                return;
            }
            pushRobDataToMq(data, httpcode, content);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "该手机号撞库异常：" + data.getCell() + "id:" + data.getId()), e);
        }
    }

    private void pushRobDataToMq(DiDiCollidingDataRob data, String httpcode, String content) {
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
            diDiV5CollidingDataLog.setCouponType(diDiV5CollidingResultResponseDTO.getData().getCouponType());
            mqJson.put("diDiV5CollidingResultResponseDTO", diDiV5CollidingResultResponseDTO);

            data.setUpdateTime(new Date());
            if ("false".equalsIgnoreCase(diDiV5CollidingDataLog.getResult()) && "1".equalsIgnoreCase(diDiV5CollidingDataLog.getFailReason())) {
                JSONObject collidingConfig = marketingCommonConfig.getDiDiV5Config();
                Long retrieveFileId = collidingConfig.getLong("retrieveFileId");

                DiDiDataLoopCycle diDiDataLoopCycle = new DiDiDataLoopCycle();
                BeanUtils.copyProperties(data, diDiDataLoopCycle);
                diDiDataLoopCycle.setSourceType("F");
                diDiDataLoopCycle.setLockType(2);
                diDiDataLoopCycle.setPackageId(retrieveFileId.toString());
                diDiDataLoopCycle.setCollidingTime(new Date(Long.parseLong(diDiV5CollidingDataLog.getNextTime())));
                diDiV5DataLoopCycleMapper.insertSelective(diDiDataLoopCycle);
            }
        }
        mqJson.put("diDiV5CollidingDataLog", diDiV5CollidingDataLog);
        log.warn("滴滴V5推送撞库日志消息体:{}", mqJson.toJSONString());
        rocketMqSwitch.syncSend(MarketingOutsideInterfaceConstants.TOPIC, MarketingOutsideInterfaceConstants.TAG_MARKETING_DIDI_V5_COLLIDING_DATA,
                mqJson.toJSONString());
    }

    private void pushCycleDataToMq(DiDiDataLoopCycle data, String httpcode, String content) {
        log.warn("滴滴V5推送撞库日志消息content:{}", content);
        JSONObject mqJson = new JSONObject();
        DiDiV5CollidingDataLog diDiV5CollidingDataLog = new DiDiV5CollidingDataLog();
        diDiV5CollidingDataLog.setApiCode(data.getApiCode());
        diDiV5CollidingDataLog.setCell(data.getCell());
        diDiV5CollidingDataLog.setHttpCode(httpcode);
        diDiV5CollidingDataLog.setDataId(data.getId());
        diDiV5CollidingDataLog.setLocalId(Long.parseLong(data.getPackageId()));
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
            diDiV5CollidingDataLog.setCouponType(diDiV5CollidingResultResponseDTO.getData().getCouponType());
            mqJson.put("diDiV5CollidingResultResponseDTO", diDiV5CollidingResultResponseDTO);

            data.setUpdateTime(new Date());
            data.setPushTime(new Date());
            if ("false".equalsIgnoreCase(diDiV5CollidingDataLog.getResult()) && "1".equalsIgnoreCase(diDiV5CollidingDataLog.getFailReason())) {
                JSONObject collidingConfig = marketingCommonConfig.getDiDiV5Config();
                Long retrieveFileId = collidingConfig.getLong("retrieveFileId");
                data.setLockType(2);
                data.setPackageId(retrieveFileId.toString());
                data.setCollidingTime(new Date(Long.parseLong(diDiV5CollidingDataLog.getNextTime())));
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