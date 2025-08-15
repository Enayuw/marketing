package com.br.marketing.service.mock.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.mock.MockCreatePolicyDTO;
import com.br.marketing.dto.mock.MockInitDTO;
import com.br.marketing.origin.CaffeineCache;
import com.br.marketing.service.mock.enums.MockNameEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName MockBaffleConfigServiceImpl
 * @Description Mock初始化
 * @Author kongbx
 * @Date 2025/6/27 17:56
 */
@Component
@Slf4j
public class MockBaffleConfigServiceImpl {

    @Resource
    private RedisChgService redisChgService;
    @Resource
    CaffeineCache caffeineCache;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private ScheduledExecutorService scheduler;
    private volatile boolean running = true;

    private static final String TITLE = "【Mock初始化】";

    @PostConstruct
    public void init() {
        try {
            scheduler = Executors.newSingleThreadScheduledExecutor();

            // 首次立即执行，后续按 interval 动态调度
            scheduler.execute(() -> {
                try {
                    checkAndUpdateMockCache();
                } catch (Exception e) {
                    log.error(TITLE + "首次轮询异常", e);
                }
                // 安排下一次
                scheduleNextRun();
            });

        } catch (Exception e) {
            log.error(TITLE + "定时任务初始化失败", e);
        }
    }

    private void scheduleNextRun() {
        if (!running) {
            return;
        }

        int interval = getValidInterval();
        scheduler.schedule(() -> {
            try {
                checkAndUpdateMockCache();
            } catch (Exception e) {
                log.error(TITLE + "轮询异常", e);
            } finally {
                // 无论成功与否，继续调度
                scheduleNextRun();
            }
        }, interval, TimeUnit.SECONDS);
    }

    private int getValidInterval() {
        Integer configInterval = marketingCommonConfig.getMockPollingInterval();
        return (configInterval != null && configInterval > 0) ? configInterval : 60;
    }

    @PreDestroy
    public void destroy() {
        // 停止后续调度
        running = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            // 禁止新任务提交，等待已提交任务完成
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn(TITLE + "Mock定时任务线程池未在5秒内优雅关闭，尝试强制关闭...");
                    scheduler.shutdownNow(); // 强制终止
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        log.error(TITLE + "Mock定时任务线程池强制关闭仍未完成！");
                    }
                }
            } catch (InterruptedException e) {
                log.error(TITLE + "Mock定时任务线程池关闭时被中断", e);
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.warn(TITLE + "Mock定时任务线程池已关闭");
        }
    }

    /**
     * 检查并更新Mock缓存
     * 比较本地缓存和Redis版本，如果不一致则更新本地缓存
     */
    private void checkAndUpdateMockCache() {
        log.warn(TITLE + "开始轮询线程更新，本地缓存："+JSON.toJSONString(caffeineCache.getAllMockLocalCache().asMap()));
        List<String> allCodes = MockNameEnum.getAllCodes();
        for (String code : allCodes) {
            String localCacheKey = RedisKeyConstant.MOCK_POLICY.concat(":" + code);
            try {
                MockInitDTO mockInitDTO = caffeineCache.getMockSwitchStatus(localCacheKey);
                String redisValue = null;
                try {
                    redisValue = redisChgService.get(localCacheKey);
                } catch (Exception e) {
                    log.warn(TITLE + "获取Redis缓存失败，key: {}", localCacheKey, e);
                }
                if (redisValue == null) {
                    if(mockInitDTO != null){
                        // 删除本地缓存
                        caffeineCache.deleteMockSwitchStatus(localCacheKey);
                        log.warn(TITLE + "删除本地缓存，key: {}", localCacheKey);
                    }
                    continue;
                }
                if (mockInitDTO == null || !isVersionConsistent(mockInitDTO, redisValue)) {
                    updateLocalCache(localCacheKey, redisValue);
                }
            } catch (Exception e) {
                log.error(TITLE + "处理Mock缓存key={}时异常", localCacheKey, e);
            }
        }
    }

    /**
     * 检查版本是否一致
     */
    private boolean isVersionConsistent(MockInitDTO mockInitDTO, String redisValue) {
        try {
            MockCreatePolicyDTO policy = JSON.parseObject(redisValue, MockCreatePolicyDTO.class);
            if (policy == null) {
                return false;
            }
            String currentVersion = policy.getVersion();
            if (StringUtils.isEmpty(currentVersion)) {
                log.warn(TITLE + "redis中版本号为空, value={}", JSONObject.toJSONString(policy));
                return false;
            }
            if (!currentVersion.equals(mockInitDTO.getVersion())) {
                return false;
            }
            if (!mockInitDTO.getEnabled().equals(policy.getEnabled())) {
                return false;
            }
        } catch (Exception e) {
            log.warn(TITLE + "反序列化MockPolicy失败, value={}", redisValue, e);
            return false;
        }
        return true;
    }

    /**
     * 更新本地缓存
     */
    private void updateLocalCache(String localCacheKey, String redisValue) {
        MockCreatePolicyDTO policy;
        try {
            policy = JSON.parseObject(redisValue, MockCreatePolicyDTO.class);
        } catch (Exception e) {
            log.warn(TITLE + "反序列化MockPolicy失败, value={}", redisValue, e);
            return;
        }
        String mockName = policy.getMockName();
        int maxRetries = 3;
        int retryCount = 0;
        while (true) {
            try {
                MockInitDTO newMockInitDTO = new MockInitDTO();
                newMockInitDTO.setMockName(mockName);
                newMockInitDTO.setEnabled(policy.getEnabled());
                newMockInitDTO.setVersion(String.valueOf(policy.getVersion()));
                caffeineCache.storeMockSwitchStatus(localCacheKey, newMockInitDTO);
                log.warn(TITLE + "成功更新Mock本地缓存，key: {}, data : {}", localCacheKey, JSONObject.toJSONString(newMockInitDTO));
                return;
            } catch (Exception e) {
                retryCount++;
                log.warn(TITLE + "更新Mock本地缓存失败，重试次数: {}/{}, key: {}", retryCount, maxRetries, localCacheKey, e);
                if (retryCount >= maxRetries) {
                    caffeineCache.deleteMockSwitchStatus(localCacheKey);
                    log.warn(TITLE + "更新Mock本地缓存失败，已达到最大重试次数，删除本地缓存，key: {}", localCacheKey);
                    return;
                }
                try {
                    Thread.sleep(1000L * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error(TITLE + "更新Mock本地缓存时线程被中断", ie);
                    return;
                }
            }
        }
    }
}
