package com.br.marketing.aspect;

import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.dto.mock.MockInitDTO;
import com.br.marketing.entity.MockCase;
import com.br.marketing.origin.CaffeineCache;
import com.br.marketing.service.mock.MockService;
import com.br.marketing.service.mock.impl.MockPolicyImpl;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
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
    CaffeineCache caffeineCache;

    @Autowired
    MockPolicyImpl mockPolicy;

    @Resource(name = "newMockService")
    private MockService mockService;

    @Around("@annotation(com.br.marketing.aspect.Mockable)")
    public Object handleCustomAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解
        Mockable mockable = method.getAnnotation(Mockable.class);
        try {
            //解析mock名称
            Object result = null;
            String mockName = mockable.mockName();
            String localCacheKey = RedisKeyConstant.MOCK_POLICY.concat(":" + mockName);
            // 获取本地缓存
            MockInitDTO localCache = caffeineCache.getMockSwitchStatus(localCacheKey);
            if (localCache != null) {
                //判断是否启用
                Integer enabled = localCache.getEnabled();
                //未启用则执行原方法
                if (enabled == 1) {
                    return joinPoint.proceed();
                }
                // 获取Redis缓存
                String redisValue = mockService.getMockRedisValue(localCacheKey);
                if (redisValue == null) {
                    return result;
                }
                //策略执行
                MockCase mockCase = mockService.action(redisValue);
                return mockCase.getResponseBody();
            }
            return result;
        } catch (Exception e) {
            log.error("方法执行异常: {}", method.getName(), e);
            throw e;
        }
    }


}
