package com.br.marketing.service.mock.impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.mock.MockInitDTO;
import com.br.marketing.entity.MockPolicy;
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

    @PostConstruct
    public void init() {
        try {
            final int interval = marketingCommonConfig.getMockPollingInterval() != null && marketingCommonConfig.getMockPollingInterval() > 0
                    ? marketingCommonConfig.getMockPollingInterval() : 60;

            scheduler = Executors.newScheduledThreadPool(2);
            scheduler.scheduleAtFixedRate(
                    () -> {
                        try {
                            checkAndUpdateMockCache();
                        } catch (Exception e) {
                            log.error("Mock轮询异常", e);
                        }
                    },
                    0, interval, TimeUnit.SECONDS
            );
            log.info("Mock定时任务已启动，轮询间隔:{}秒，线程池:2线程", interval);
        } catch (Exception e) {
            log.warn("Mock定时任务初始化异常", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Mock定时任务线程池已关闭");
        }
    }

    /**
     * 检查并更新Mock缓存
     * 比较本地缓存和Redis版本，如果不一致则更新本地缓存
     */
    private void checkAndUpdateMockCache() {
        List<String> allCodes = MockNameEnum.getAllCodes();
        for (String code : allCodes) {
            String localCacheKey = RedisKeyConstant.MOCK_POLICY.concat(":" + code);
            try {
                MockInitDTO mockInitDTO = caffeineCache.getMockSwitchStatus(localCacheKey);
                String redisValue = null;
                try {
                    redisValue = redisChgService.get(localCacheKey);
                } catch (Exception e) {
                    log.warn("获取Redis缓存失败，key: {}", localCacheKey, e);
                }
                if (redisValue == null) {
                    continue;
                }
                if (mockInitDTO == null || !isVersionConsistent(mockInitDTO, redisValue)) {
                    updateLocalCache(localCacheKey, redisValue);
                }
            } catch (Exception e) {
                log.error("处理Mock缓存key={}时异常", localCacheKey, e);
            }
        }
    }

    /**
     * 检查版本是否一致
     */
    private boolean isVersionConsistent(MockInitDTO mockInitDTO, String redisValue) {
        MockPolicy policy = null;
        try {
            policy = JSON.parseObject(redisValue, MockPolicy.class);
        } catch (Exception e) {
            log.warn("反序列化MockPolicy失败, value={}", redisValue, e);
            return false;
        }
        if (mockInitDTO == null || policy == null) {
            return false;
        }
        String currentVersion = policy.getVersion();
        if(StringUtils.isEmpty(currentVersion)){
            return false;
        }

        if (!currentVersion.equals(mockInitDTO.getVersion())) {
            return false;
        }
        if (!mockInitDTO.getEnabled().equals(policy.getEnabled())) {
            return false;
        }
        return true;
    }

    /**
     * 更新本地缓存
     */
    private void updateLocalCache(String localCacheKey, String redisValue) {
        MockPolicy policy = null;
        try {
            policy = JSON.parseObject(redisValue, MockPolicy.class);
        } catch (Exception e) {
            log.warn("反序列化MockPolicy失败, value={}", redisValue, e);
            return;
        }
        String mockName = localCacheKey.substring(localCacheKey.lastIndexOf(":") + 1);
        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                MockInitDTO newMockInitDTO = new MockInitDTO();
                newMockInitDTO.setMockName(mockName);
                newMockInitDTO.setEnabled(policy.getEnabled());
                newMockInitDTO.setVersion(String.valueOf(policy.getVersion()));
                caffeineCache.storeMockSwitchStatus(localCacheKey, newMockInitDTO);
                log.info("成功更新Mock本地缓存，key: {}, version: {}", localCacheKey, newMockInitDTO.getVersion());
                return;
            } catch (Exception e) {
                retryCount++;
                log.warn("更新Mock本地缓存失败，重试次数: {}/{}, key: {}", retryCount, maxRetries, localCacheKey, e);
                if (retryCount >= maxRetries) {
                    caffeineCache.deleteMockSwitchStatus(localCacheKey);
                    log.error("更新Mock本地缓存失败，已达到最大重试次数，删除本地缓存，key: {}", localCacheKey);
                    return;
                }
                try {
                    Thread.sleep(1000L * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("更新Mock本地缓存时线程被中断", ie);
                    return;
                }
            }
        }
    }
}
