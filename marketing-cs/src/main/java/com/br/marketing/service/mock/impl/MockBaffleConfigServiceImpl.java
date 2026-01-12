package com.br.marketing.service.mock.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.mock.MarketingMockApiService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.constants.MockConstants;
import com.br.marketing.dto.mock.MockCreatePolicyDTO;
import com.br.marketing.dto.mock.MockInitDTO;
import com.br.marketing.origin.CaffeineCache;
import com.br.marketing.service.mock.MockService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
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
    CaffeineCache caffeineCache;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private ScheduledExecutorService scheduler;
    private volatile boolean running = true;

    @Resource
    private MarketingMockApiService marketingMockApiService;
    
    @Resource(name = "newMockService")
    private MockService mockService;

    // 获取应用名称，用于判断是否需要启用Mock功能
    @Value("${spring.application.name:unknown}")
    private String applicationName;

    private static final String TITLE = "【Mock初始化】";
    
    /** inner-api 项目名称，该项目可直接调用 MockService 查询 Redis，无需走 HTTP API */
    private static final String INNER_API_PROJECT = "marketing-inner-api";

    @PostConstruct
    public void init() {
        // 检查当前项目是否需要禁用Mock初始化
        Set<String> disableMockProjects = marketingCommonConfig.getDisableMockProjects();
        if (!disableMockProjects.contains(applicationName)) {
            log.warn(TITLE + "当前项目 [{}] 不在可用Mock列表中，跳过Mock，执行真实方法", applicationName);
            return;
        }
        
        log.warn(TITLE + "项目 [{}] 开始初始化Mock功能", applicationName);
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
     * 根据项目类型查询 Mock 配置
     * inner-api 项目直接调用 MockService（本地 Redis），其他项目走 HTTP API
     *
     * @param cacheKey Redis 缓存 key
     * @return Mock 配置结果
     */
    private Result<String> queryMockConfigByProject(String cacheKey) {
        if (INNER_API_PROJECT.equals(applicationName)) {
            // inner-api 项目直接调用 MockService 查询 Redis，避免 HTTP 自调用
            return mockService.queryMockConfig(cacheKey);
        }
        // 其他项目通过 HTTP API 调用 inner-api
        return marketingMockApiService.queryMockConfig(cacheKey);
    }

    /**
     * 检查并更新Mock缓存
     * 比较本地缓存和Redis版本，如果不一致则更新本地缓存
     */
    private void checkAndUpdateMockCache() {
        log.warn(TITLE + "开始轮询线程更新，本地缓存："+JSON.toJSONString(caffeineCache.getAllMockLocalCache().asMap()));
        List<String> allMockNames = MockConstants.getAllMockNames();
        for (String mockName : allMockNames) {
            String localCacheKey = RedisKeyConstant.MOCK_POLICY.concat(":" + mockName);
            try {
                // 获取本地缓存
                MockInitDTO mockInitDTO = caffeineCache.getMockSwitchStatus(localCacheKey);
                String mockConfigValue = null;

                // 查询mock配置信息
                // inner-api 项目直接调用 MockService，其他项目走 HTTP API
                Result<String> mockConfig = queryMockConfigByProject(localCacheKey);
                Integer code = mockConfig.getCode();
                if(code.equals(ResultCode.SUCCESS.getValue())){
                    mockConfigValue = mockConfig.getData();
                }

                if (mockConfigValue == null) {
                    // 本地缓存存在 但 redis-db中不存在
                    if(mockInitDTO != null){
                        // 删除本地缓存
                        caffeineCache.deleteMockSwitchStatus(localCacheKey);
                        log.warn(TITLE + "删除本地缓存，key: {}", localCacheKey);
                    }
                    continue;
                }
                // 存在mock配置，对比版本号
                if (mockInitDTO == null || !isVersionConsistent(mockInitDTO, mockConfigValue)) {
                    updateLocalCache(localCacheKey, mockConfigValue);
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
