package com.br.marketing.aspect;

import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.dto.mock.MockInitDTO;
import com.br.marketing.entity.MockCase;
import com.br.marketing.origin.CaffeineCache;
import com.br.marketing.service.mock.MockService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @ClassName MockableAspect
 * @Description Mock挡板
 * @Author kongbx
 * @Date 2025/6/27 10:45
 */
@Slf4j
@Aspect
@Order(-995)
@Component
public class MockableAspect {

    @Resource
    private CaffeineCache caffeineCache;

    @Resource(name = "newMockService")
    private MockService mockService;

    /**
     * 拦截带有 @Mockable 注解的方法，动态决定是否走Mock逻辑
     */
    @Around("@annotation(mockable)")
    public Object handleMockableMethod(ProceedingJoinPoint joinPoint, Mockable mockable) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getName();  // 记录方法名，便于日志追踪

        try {
            String mockName = mockable.mockName();
            String cacheKey = RedisKeyConstant.MOCK_POLICY + ":" + mockName;  // 构造缓存Key

            // 1. 先查本地缓存（Caffeine），命中且 enabled=1 则直接执行原方法
            MockInitDTO localCache = caffeineCache.getMockSwitchStatus(cacheKey);
            if (localCache != null && localCache.getEnabled() == 1) {
                return joinPoint.proceed();
            }

            // 2. 本地缓存未命中或未启用，则查Redis
            String redisMockConfig = mockService.getMockRedisValue(cacheKey);
            if (redisMockConfig != null) {
                MockCase mockCase = mockService.action(redisMockConfig);
                if (mockCase != null) {
                    return mockCase.getResponseBody();
                }
            }

            // 3. 默认情况：执行原方法
            return joinPoint.proceed();

        } catch (Exception e) {
            log.error("【Mock拦截异常】方法 {} 执行失败，原因：{}", methodName, e.getMessage(), e);
            throw e;
        }
    }
}
