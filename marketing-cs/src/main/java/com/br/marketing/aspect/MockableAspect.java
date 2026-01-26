package com.br.marketing.aspect;

import com.alibaba.fastjson2.JSON;
import com.br.marketing.client.mock.MarketingMockApiService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.mock.MockCreateCaseDTO;
import com.br.marketing.dto.mock.MockCreatePolicyDTO;
import com.br.marketing.dto.mock.MockInitDTO;
import com.br.marketing.origin.CaffeineCache;
import com.br.marketing.service.mock.MockService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    @Resource
    private MarketingMockApiService marketingMockApiService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private ObjectMapper objectMapper;

    // 获取应用名称，用于判断是否需要启用Mock功能
    @Value("${spring.application.name:unknown}")
    private String applicationName;

    private final String TITLE = "【mock切面】";
    
    /** inner-api 项目名称，该项目可直接调用 MockService 查询 Redis，无需走 HTTP API */
    private static final String INNER_API_PROJECT = "marketing-inner-api";

    /**
     * 拦截带有 @Mockable 注解的方法，动态决定是否走Mock逻辑
     */
    @Around("@annotation(mockable)")
    public Object handleMockableMethod(ProceedingJoinPoint joinPoint, Mockable mockable) throws Throwable {
        // 检查当前项目是否需要禁用Mock初始化
        Set<String> disableMockProjects = marketingCommonConfig.getDisableMockProjects();
        if (!disableMockProjects.contains(applicationName)) {
            log.warn(TITLE + "当前项目 [{}] 不在可用Mock列表中，跳过Mock，执行真实方法", applicationName);
            return joinPoint.proceed();
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> returnType = method.getReturnType();
        String methodName = method.getName();

        try {
            String mockName = mockable.mockName();
            String cacheKey = RedisKeyConstant.MOCK_POLICY + ":" + mockName;

            // 查询本地缓存
            MockInitDTO localCache = caffeineCache.getMockSwitchStatus(cacheKey);

            // 本地缓存中mock开关状态为关闭
            if (localCache != null && localCache.getEnabled() == 1) {
                return joinPoint.proceed();
            }

            // 本地缓存为空 或者 开关状态为开启，则查询redis-db
            // inner-api 项目直接调用 MockService，其他项目走 HTTP API
            Result<String> mockRedisValue = queryMockConfigByProject(cacheKey);
            Integer code = mockRedisValue.getCode();
            // 查询redis为空  || redis异常后查询DB为空
            if(!code.equals(ResultCode.SUCCESS.getValue())){
                return joinPoint.proceed();
            }

            // 查询到挡板配置
            String redisValue = mockRedisValue.getData();

            // 挡板关闭，执行真实方法
            MockCreatePolicyDTO policy = JSON.parseObject(redisValue, MockCreatePolicyDTO.class);
            if(policy.getEnabled().equals(1)){
                return joinPoint.proceed();
            }

            MockCreateCaseDTO mockCase = mockService.action(policy);
            // 未配置测试用例，返回空对象
            if (mockCase == null) {
                return new Object();
            }
            // 解析测试用例
            String responseBodyStr = mockCase.getResponseBody();
            // 1、先将JSON字符串解析为对象
            Object responseBody = parseResponseBody(responseBodyStr, methodName);
            // 2、根据方法返回类型适配响应
            return adaptResponseToReturnType(responseBody, returnType, method, methodName);

        } catch (Exception e) {
            log.error(TITLE + "【拦截异常】方法 {} 执行失败，原因：{}", methodName, e.getMessage(), e);
            throw e;
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
     * 解析Mock响应体JSON字符串为对象
     * @param responseBodyStr JSON字符串
     * @param methodName 方法名（用于日志）
     * @return 解析后的对象
     */
    private Object parseResponseBody(String responseBodyStr, String methodName) {
        if (StringUtils.isEmpty(responseBodyStr)) {
            return null;
        }
        
        try {
            // 尝试解析为JSON对象
            return objectMapper.readValue(responseBodyStr, Object.class);
        } catch (Exception e) {
            log.warn(TITLE + "【JSON解析失败】方法 {} 无法解析响应体JSON，返回原始字符串。JSON: {}, 错误：{}",
                    methodName, responseBodyStr, e.getMessage());
            // 解析失败时返回原始字符串
            return responseBodyStr;
        }
    }

    /**
     * 根据方法返回类型适配响应数据
     * @param responseBody Mock响应数据
     * @param returnType 方法返回类型
     * @param method 目标方法
     * @param methodName 方法名（用于日志）
     * @return 适配后的响应对象
     */
    private Object adaptResponseToReturnType(Object responseBody, Class<?> returnType, Method method, String methodName) {
        try {
            // 处理 void 类型
            if (Void.TYPE.equals(returnType)) {
                return null;
            }

            // 处理 ApiResult 类型
            if (ApiResult.class.isAssignableFrom(returnType)) {
                try {
                    return objectMapper.convertValue(responseBody, objectMapper.getTypeFactory().constructType(method.getGenericReturnType()));
                } catch (Exception e) {
                    log.warn(TITLE + "【ApiResult转换】方法 {} 响应体无法直接转换为ApiResult，错误：{}", methodName, e.getMessage());
                    return new ApiResult<>().success(responseBody);
                }
            }

            // 处理 Result 类型
            if (Result.class.isAssignableFrom(returnType)) {
                try {
                    return objectMapper.convertValue(responseBody, objectMapper.getTypeFactory().constructType(method.getGenericReturnType()));
                } catch (Exception e) {
                    log.warn(TITLE + "【Result转换】方法 {} 响应体无法直接转换为Result，错误：{}", methodName, e.getMessage());
                    Result<Object> result = new Result<>();
                    result.success();
                    result.setDate(responseBody);
                    return result;
                }
            }

            // 处理 List 类型
            if (java.util.List.class.isAssignableFrom(returnType)) {
                return adaptResponseToListType(responseBody, method, methodName);
            }

            // 如果响应体为 null，直接返回 null
            if (responseBody == null) {
                return null;
            }

            // 如果返回类型就是 Object，直接返回
            if (Object.class.equals(returnType)) {
                return responseBody;
            }

            // 如果响应体已经是目标类型的实例，直接返回
            if (returnType.isInstance(responseBody)) {
                return responseBody;
            }

            // 对于自定义DTO类型，使用更灵活的类型转换
            return convertToTargetType(responseBody, method.getGenericReturnType(), returnType, methodName);

        } catch (Exception e) {
            log.warn(TITLE + "【类型适配失败】方法 {} 无法将响应数据适配为 {} 类型，返回原始数据。错误：{}",
                    methodName, returnType.getSimpleName(), e.getMessage());
            return responseBody;
        }
    }

    /**
     * 灵活的类型转换方法，支持泛型类型
     * @param responseBody Mock响应数据
     * @param genericType 泛型类型信息
     * @param rawType 原始类型
     * @param methodName 方法名（用于日志）
     * @return 转换后的对象
     */
    private Object convertToTargetType(Object responseBody, Type genericType, Class<?> rawType, String methodName) {
        try {
            // 如果有泛型信息，使用泛型信息进行转换
            if (genericType instanceof ParameterizedType) {
                return objectMapper.readValue(
                    objectMapper.writeValueAsString(responseBody),
                    objectMapper.getTypeFactory().constructType(genericType)
                );
            }
            
            // 否则使用普通的类型转换
            return objectMapper.convertValue(responseBody, rawType);
            
        } catch (Exception e) {
            log.warn(TITLE + "【泛型类型转换失败】方法 {} 无法将响应数据转换为 {} 类型，尝试普通转换。错误：{}",
                    methodName, rawType.getSimpleName(), e.getMessage());
            
            try {
                // 降级到普通类型转换
                return objectMapper.convertValue(responseBody, rawType);
            } catch (Exception e2) {
                log.warn(TITLE + "【普通类型转换失败】方法 {} 无法将响应数据转换为 {} 类型，返回原始数据。错误：{}",
                        methodName, rawType.getSimpleName(), e2.getMessage());
                return responseBody;
            }
        }
    }

    /**
     * 处理List类型的响应适配
     * @param responseBody Mock响应数据
     * @param method 目标方法
     * @param methodName 方法名（用于日志）
     * @return 适配后的List对象
     */
    private Object adaptResponseToListType(Object responseBody, Method method, String methodName) {
        try {
            // 如果响应体为null，返回空List
            if (responseBody == null) {
                return new ArrayList<>();
            }
            
            // 使用通用的泛型转换方法处理List类型
            return convertToTargetType(responseBody, method.getGenericReturnType(), method.getReturnType(), methodName);
                
        } catch (Exception e) {
            log.warn(TITLE + "【List类型适配失败】方法 {} 无法将响应数据适配为List类型，返回空List。错误：{}",
                    methodName, e.getMessage());
            return new ArrayList<>();
        }
    }


}
