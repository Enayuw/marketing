package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.mock.MockInitDTO;
import com.br.marketing.service.mock.enums.MockNameEnum;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
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

    // 本地缓存
    private Cache<String, Object> localCache;

    @Resource
    private RedisChgService redisChgService;

    ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10);

    @PostConstruct
    private void init() {
        // 初始化本地缓存
        localCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();

        List<Integer> allCodes = MockNameEnum.getAllCodes();

        for (Integer code : allCodes) {
            threadPool.submit(() -> checkAndUpdateCache(code));
        }
    }

    /**
     * 检查并更新缓存
     *
     * @param cacheKey 缓存键
     */
    private void checkAndUpdateCache(Integer cacheKey) {
        try {

            String mockKey = RedisKeyConstant.MOCK_POLICY.concat(":" + cacheKey);

            //获取本地缓存数据
            Object ifPresent = localCache.getIfPresent(mockKey);
            if (ifPresent == null) {
                // 获取Redis中数据
                String mock = redisChgService.get(mockKey);
                if (!StringUtils.isEmpty(mock)) {
                    localCache.put(mockKey, mock);
                }
            }
            MockInitDTO uploadJson = JSON.parseObject((String) ifPresent, MockInitDTO.class);

        } catch (Exception e) {
            log.error("缓存一致性检查异常，cacheKey: {}", cacheKey, e);
        }
    }

}
