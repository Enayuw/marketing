package com.br.marketing.aspect;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.dto.mock.MockCreateCaseDTO;
import com.br.marketing.dto.mock.MockInitDTO;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 拦截带有 @Mockable 注解的方法，动态决定是否走Mock逻辑
     */
    @Around("@annotation(mockable)")
    public Object handleMockableMethod(ProceedingJoinPoint joinPoint, Mockable mockable) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> returnType = method.getReturnType();
        String methodName = method.getName();

        try {
            String mockName = mockable.mockName();
            String cacheKey = RedisKeyConstant.MOCK_POLICY + ":" + mockName;

            MockInitDTO localCache = caffeineCache.getMockSwitchStatus(cacheKey);
            if (localCache != null && localCache.getEnabled() == 1) {
                return joinPoint.proceed();
            }

            String redisMockConfig = mockService.getMockRedisValue(cacheKey);
            if (redisMockConfig != null) {
                MockCreateCaseDTO mockCase = mockService.action(redisMockConfig);
                if (mockCase != null) {
                    Object responseBody = mockCase.getResponseBody();
                    // 根据方法返回类型适配响应
                    return adaptResponseToReturnType(responseBody, returnType, methodName);
                }
            }
            return joinPoint.proceed();

        } catch (Exception e) {
            log.error("【Mock拦截异常】方法 {} 执行失败，原因：{}", methodName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据方法返回类型适配响应数据
     * @param responseBody Mock响应数据
     * @param returnType 方法返回类型
     * @param methodName 方法名（用于日志）
     * @return 适配后的响应对象
     */
    private Object adaptResponseToReturnType(Object responseBody, Class<?> returnType, String methodName) {
        try {
            // 处理 void 类型
            if (Void.TYPE.equals(returnType)) {
                return null;
            }

            // 处理 ApiResult 类型
            if (ApiResult.class.isAssignableFrom(returnType)) {
                return new ApiResult<>().success(responseBody);
            }

            // 处理 Result 类型
            if (Result.class.isAssignableFrom(returnType)) {
                Result<Object> result = new Result<>();
                result.success();
                result.setDate(responseBody);
                return result;
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

            // 处理基本类型和包装类型
            if (returnType.isPrimitive() || isWrapperType(returnType)) {
                return convertToPrimitiveOrWrapper(responseBody, returnType);
            }

            // 处理字符串类型
            if (String.class.equals(returnType)) {
                return responseBody.toString();
            }

            // 对于复杂对象类型，尝试使用 Jackson 进行类型转换
            return objectMapper.convertValue(responseBody, returnType);

        } catch (Exception e) {
            log.warn("【Mock类型适配失败】方法 {} 无法将响应数据适配为 {} 类型，返回原始数据。错误：{}",
                    methodName, returnType.getSimpleName(), e.getMessage());
            return responseBody;
        }
    }

    /**
     * 判断是否为包装类型
     */
    private boolean isWrapperType(Class<?> clazz) {
        return clazz == Boolean.class || clazz == Byte.class || clazz == Character.class ||
                clazz == Short.class || clazz == Integer.class || clazz == Long.class ||
                clazz == Float.class || clazz == Double.class;
    }

    /**
     * 转换为基本类型或包装类型
     */
    private Object convertToPrimitiveOrWrapper(Object value, Class<?> targetType) {
        if (value == null) {
            return getDefaultValue(targetType);
        }

        String stringValue = value.toString();

        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(stringValue);
        } else if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(stringValue);
        } else if (targetType == char.class || targetType == Character.class) {
            return !stringValue.isEmpty() ? stringValue.charAt(0) : '\0';
        } else if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(stringValue);
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(stringValue);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(stringValue);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(stringValue);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(stringValue);
        }

        return value;
    }

    /**
     * 获取基本类型的默认值
     */
    private Object getDefaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        return null;
    }
}
