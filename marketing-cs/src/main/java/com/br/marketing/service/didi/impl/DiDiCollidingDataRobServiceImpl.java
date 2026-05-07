package com.br.marketing.service.didi.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.didi.DiDiV5Client;
import com.br.marketing.client.didi.input.v5.DiDiV5CollidingRequestDTO;
import com.br.marketing.client.didi.output.v5.DiDiV5CollidingResultResponseDTO;
import com.br.marketing.client.didi.utils.MD5Util;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.DiDiCollidingDataRob;
import com.br.marketing.entity.DiDiV5CollidingDataLog;
import com.br.marketing.mapper.DiDiV5CollidingDataRobMapper;
import com.br.marketing.mapper.DiDiV5DataLoopCycleMapper;
import com.br.marketing.service.didi.DiDiCollidingDataRobService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DiDiCollidingDataRobServiceImpl implements DiDiCollidingDataRobService {

    private final static String REDIS_KEY= "lock:collidingData:didiV5";

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DiDiV5CollidingDataRobMapper diDiV5CollidingDataRobMapper;

    @Resource
    private DiDiV5Client diDiV5Client;

    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Override
    public void colliding(JobExecutionMultipleShardingContext context) {
        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.DIDI_V5_COLLIDING.getName(), 200, 200);
        JSONObject collidingConfig = marketingCommonConfig.getDiDiV5Config();
        RateLimiter rateLimiter = RateLimiter.create(collidingConfig.getInteger("rateLimit"));

        String startTimeStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)) + " "
                + collidingConfig.getString("firstBatchStartTime");
        String endTimeStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)) + " "
                + collidingConfig.getString("firstBatchEndTime");
        DateTime startTime = DateUtil.parse(startTimeStr);
        DateTime endTime = DateUtil.parse(endTimeStr);

        String mediaName = collidingConfig.getString("mediaName") != null ? collidingConfig.getString("mediaName") : "bairongC";
        String token = collidingConfig.getString("token") != null ? collidingConfig.getString("token") : "9Hqeoi36CJfdA7n4";

        // 处理非周期锁定的数据
        processRobData(mediaName, token, rateLimiter,
                pushPool, 2, startTime, endTime);
        processRobData(mediaName, token, rateLimiter,
                pushPool, 3, startTime, endTime);
        pushPool.shutdownAndAwaitTermination();
    }

    private long getLastId(int priority) {
        String redisKey = REDIS_KEY + ":" + priority;
        String id = redisChgService.get(redisKey);
        return id == null ? 0 : Long.parseLong(id);
    }

    private void freshRedisId(long id, int priority) {
        String redisKey = REDIS_KEY + ":" + priority;
        redisChgService.setex(redisKey, String.valueOf(id), 6 * 3600);
    }

    private void processRobData(String mediaName, String token, RateLimiter rateLimiter,
                                TpDynamicExecutor pushPool, int priority, Date startTime, Date endTime) {
        // 处理非周期锁定的数据
        long maxId = getLastId(priority);
        while (true) {
            JSONObject collidingConfig = marketingCommonConfig.getDiDiV5Config();
            boolean collidingSwitch = collidingConfig.getBoolean("collidingSwitch");
            if (collidingSwitch) {
                break;
            }

            if (DateUtil.compare(new Date(), endTime) >= 0 || DateUtil.compare(new Date(), startTime) < 0) {
                break;
            }
            Integer threadNum = collidingConfig.getInteger("threadNum");
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pushPool, threadNum);
            int limit = collidingConfig.getInteger("limit") != null ? collidingConfig.getInteger("limit") : 2000;
            List<DiDiCollidingDataRob> dataList;
            if (priority == 2) {
                dataList = diDiV5CollidingDataRobMapper.queryCollidingDataIdx(limit, startTime, new Date(), maxId);
            } else {
                dataList = diDiV5CollidingDataRobMapper.queryUploadedDataIdx(limit, DateUtil.beginOfDay(new Date()), new Date(), maxId);
            }

            if (CollectionUtils.isEmpty(dataList)) {
                break;
            }
            maxId = dataList.get(dataList.size() - 1).getId();
            freshRedisId(maxId, priority);
            boolean rateLimitSwitch = collidingConfig.getBoolean("rateLimitSwitch");
            Integer mockEnable = collidingConfig.getInteger("mockEnable");
            dataList.forEach(data -> pushPool.submit(() -> collidingData(data, mediaName, token, rateLimiter, rateLimitSwitch, mockEnable)));
        }
    }

    private void collidingData(DiDiCollidingDataRob data, String mediaName, String token, RateLimiter rateLimiter,
                               boolean rateLimitSwitch, Integer mockEnable) {
        // 单个撞库异常不影响其他撞库
        try {
            // 尝试获取令牌
            if(rateLimitSwitch) {
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
            }
            String resData;
            if (mockEnable.equals(1)) {
                Thread.sleep(100);
                resData = "{\"httpcode\":200,\"content\":{\"errorCode\":10000,\"errorMessage\":\"成功\",\"data\":" +
                        "{\"couponType\":\"60free\",\"result\":true,\"msgType\":null,\"failReason\":null,\"userGroup\":3," +
                        "\"nextTime\":1769327333000}}}";
            } else if(mockEnable.equals(2)){
                Result<String> response = diDiV5Client.colliding(mediaName, buildRequest(data.getCell(), token));
                resData = response.getData();
            } else {
                Result<String> response = diDiV5Client.collidingWithoutMock(mediaName, buildRequest(data.getCell(), token));
                resData = response.getData();
            }
            JSONObject resJson = JSONObject.parseObject(resData);
            String httpcode = resJson.getString("httpcode");
            String content = resJson.getString("content");
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
        diDiV5CollidingDataLog.setSourceType(data.getSourceType());
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
            diDiV5CollidingDataLog.setSourceType(data.getSourceType());
            mqJson.put("diDiV5CollidingResultResponseDTO", diDiV5CollidingResultResponseDTO);

            data.setUpdateTime(new Date());
        }
        mqJson.put("diDiV5CollidingDataLog", diDiV5CollidingDataLog);
        log.warn("滴滴V5推送撞库日志消息体:{}", mqJson.toJSONString());
        rocketMqSwitch.syncSend(MarketingOutsideInterfaceConstants.TOPIC, MarketingOutsideInterfaceConstants.TAG_MARKETING_DIDI_V5_COLLIDING_DATA,
                mqJson.toJSONString());
    }

    private DiDiV5CollidingRequestDTO buildRequest(String cell, String token) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        // todo MD5Util改成用JDK的MD5工具，得验证一下，最后改这个
        return new DiDiV5CollidingRequestDTO().setSign(cell).setTimestamp(timestamp).setSignature(MD5Util.encode(cell + timestamp + token));
    }
}