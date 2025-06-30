package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MockLocalCache;
import com.br.marketing.entity.MockPolicy;
import com.br.marketing.origin.CaffeineCache;
import com.br.marketing.service.mock.enums.MockNameEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

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

    ThreadPoolExecutor pool = BrExecutors.getThreadPool(10, 10);

    @PostConstruct
    public void init() throws InterruptedException {
        try {
            final int interval = marketingCommonConfig.getMockPollingInterval() != null && marketingCommonConfig.getMockPollingInterval() > 0
                    ? marketingCommonConfig.getMockPollingInterval() : 60;

            List<Integer> allCodes = MockNameEnum.getAllCodes();
            for (Integer code : allCodes) {
                pool.submit(() -> checkAndUpdateMockCache(code));

                ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                        () -> {
                            try {
                                checkAndUpdateMockCache(code);
                            } catch (Exception e) {
                                log.error("Mock轮询异常", e);
                            }
                        },
                        0, interval, TimeUnit.SECONDS
                );
            }
        } catch (Exception e) {
            log.warn("");
        }
    }

    /**
     * 检查并更新Mock缓存
     * 比较本地缓存和Redis版本，如果不一致则更新本地缓存
     */
    private void checkAndUpdateMockCache(Integer code) {
        try {
            String localCacheKey = RedisKeyConstant.MOCK_POLICY.concat(":" + code);
            // 获取本地缓存
            MockLocalCache localCache = caffeineCache.getMockSwitchStatus(localCacheKey);

            // 获取Redis缓存
            String redisValue = null;
            try {
                redisValue = redisChgService.get(localCacheKey);
            } catch (Exception e) {
                log.warn("获取Redis缓存失败，key: {}", localCacheKey, e);
            }
            //如果redis查询为空 则返回
            if (redisValue == null) {
                return;
            }

            // 比较版本
            if (localCache == null || !isVersionConsistent(localCache, redisValue)) {
                // 版本不一致，更新本地缓存
                updateLocalCache(localCacheKey, redisValue);
            }

        } catch (Exception e) {
            log.error("检查Mock缓存版本时发生异常", e);
            throw e;
        }
    }

    /**
     * 检查版本是否一致
     */
    private boolean isVersionConsistent(MockLocalCache localCache, String redisValue) {
        MockPolicy policy = JSON.parseObject(redisValue, MockPolicy.class);
        if (localCache == null || policy == null) {
            return false;
        }

        // 比较版本号（使用ID作为版本号）
        String currentVersion = policy.getVersion();
        if (!currentVersion.equals(localCache.getVersion())) {
            return false;
        }

        // 比较启用状态
        if (!localCache.getEnabled().equals(policy.getEnabled())) {
            return false;
        }

        // 比较更新时间
        if (!localCache.getUpdateTime().equals(policy.getUpdateTime())) {
            return false;
        }

        return true;
    }

    /**
     * 更新本地缓存
     */
    private void updateLocalCache(String localCacheKey, String redisValue) {
        MockPolicy policy = JSON.parseObject(redisValue, MockPolicy.class);

        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                // 创建新的本地缓存对象
                MockLocalCache newLocalCache = new MockLocalCache();
                newLocalCache.setEnabled(policy.getEnabled());
                newLocalCache.setVersion(String.valueOf(policy.getVersion()));
                newLocalCache.setUpdateTime(policy.getUpdateTime());

                // 更新本地缓存
                caffeineCache.storeMockSwitchStatus(localCacheKey, newLocalCache);

                log.info("成功更新Mock本地缓存，key: {}, version: {}", localCacheKey, newLocalCache.getVersion());
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
                    Thread.sleep(1000 * retryCount); // 递增等待时间
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("更新Mock本地缓存时线程被中断", ie);
                }
            }
        }
    }

}
