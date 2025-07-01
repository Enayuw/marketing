package com.br.marketing.service.mock.impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.dto.mock.MockInitDTO;
import com.br.marketing.entity.MockPolicy;
import com.br.marketing.origin.CaffeineCache;
import com.br.marketing.service.mock.enums.MockNameEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    
    @PostConstruct
    public void init() {
        try {
            final int interval = marketingCommonConfig.getMockPollingInterval() != null && marketingCommonConfig.getMockPollingInterval() > 0
                    ? marketingCommonConfig.getMockPollingInterval() : 60;

            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                    () -> {
                        try {
                            checkAndUpdateMockCache();
                        } catch (Exception e) {
                            log.error("Mock轮询异常", e);
                        }
                    },
                    0, interval, TimeUnit.SECONDS
            );

        } catch (Exception e) {
            log.warn("");
        }
    }

    /**
     * 检查并更新Mock缓存
     * 比较本地缓存和Redis版本，如果不一致则更新本地缓存
     */
    private void checkAndUpdateMockCache() {
        try {

            List<String> allCodes = MockNameEnum.getAllCodes();

            for (String code : allCodes) {
                String localCacheKey = RedisKeyConstant.MOCK_POLICY.concat(":" + code);
                // 获取本地缓存
                MockInitDTO mockInitDTO = caffeineCache.getMockSwitchStatus(localCacheKey);

                // 获取Redis缓存
                String redisValue = null;
                try {
                    redisValue = redisChgService.get(localCacheKey);
                } catch (Exception e) {
                    log.warn("获取Redis缓存失败，key: {}", localCacheKey, e);
                }
                //如果redis查询为空 则返回
                if (redisValue == null) {
                    continue;
                }

                // 比较版本
                if (mockInitDTO == null || !isVersionConsistent(mockInitDTO, redisValue)) {
                    // 版本不一致，更新本地缓存
                    updateLocalCache(localCacheKey, redisValue);
                }
            }

        } catch (Exception e) {
            log.error("检查Mock缓存版本时发生异常", e);
            throw e;
        }
    }

    /**
     * 检查版本是否一致
     */
    private boolean isVersionConsistent(MockInitDTO mockInitDTO, String redisValue) {
        MockPolicy policy = JSON.parseObject(redisValue, MockPolicy.class);
        if (mockInitDTO == null || policy == null) {
            return false;
        }

        // 比较版本号（使用ID作为版本号）
        String currentVersion = policy.getVersion();
        if (!currentVersion.equals(mockInitDTO.getVersion())) {
            return false;
        }

        // 比较启用状态
        if (!mockInitDTO.getEnabled().equals(policy.getEnabled())) {
            return false;
        }

        return true;
    }

    /**
     * 更新本地缓存
     */
    private void updateLocalCache(String localCacheKey, String redisValue) {
        MockPolicy policy = JSON.parseObject(redisValue, MockPolicy.class);
        String mockName = localCacheKey.substring(localCacheKey.lastIndexOf(":"));

        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                // 创建新的本地缓存对象
                MockInitDTO newMockInitDTO = new MockInitDTO();
                newMockInitDTO.setMockName(mockName);
                newMockInitDTO.setEnabled(policy.getEnabled());
                newMockInitDTO.setVersion(String.valueOf(policy.getVersion()));

                // 更新本地缓存
                caffeineCache.storeMockSwitchStatus(localCacheKey, newMockInitDTO);

                log.info("成功更新Mock本地缓存，key: {}, version: {}", localCacheKey, newMockInitDTO.getVersion());
                return; // 更新成功，退出重试循环

            } catch (Exception e) {
                retryCount++;
                log.warn("更新Mock本地缓存失败，重试次数: {}/{}, key: {}", retryCount, maxRetries, localCacheKey, e);

                if (retryCount >= maxRetries) {
                    // 达到最大重试次数，删除本地缓存并抛出异常
                    caffeineCache.deleteMockSwitchStatus(localCacheKey);
                    log.error("更新Mock本地缓存失败，已达到最大重试次数，删除本地缓存，key: {}", localCacheKey);
                    throw new RuntimeException("更新Mock本地缓存失败", e);
                }

                // 等待一段时间后重试
                try {
                    Thread.sleep(1000L * retryCount); // 递增等待时间
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("更新Mock本地缓存时线程被中断", ie);
                }
            }
        }
    }

}
