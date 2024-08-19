package com.br.marketing.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.br.marketing.context.ThreadApicodeInfo;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.google.common.base.Splitter;

/**
 * 用户权限拦截器
 */
@Component
@Aspect
public class AuthPermissionsAspect {

    /**
     * 方法
     *
     * @param
     * @return
     */
    @Pointcut("@annotation(com.br.marketing.mysqlInterceptor.AddDataAuthBusiness)")
    public void pointCut() {

    }

    /**
     * 前置调用
     *
     * @param
     * @return
     */
    @Before("pointCut()")
    public void before() {
        MarketingUserDetail user = ThreadContextInfo.getUser();
        if (user != null) {
            List adminUser = user.getRoleList().stream().filter(marketingRole -> marketingRole.getId() == 1).collect(Collectors.toList());
            // 超级管理员 跳过
            if (!CollectionUtils.isEmpty(adminUser)) {
                return;
            }
            ThreadApicodeInfo.setData(user.getApiCode());
        }
    }

    /**
     * 后置调用
     *
     * @param
     * @return
     */
    @After("pointCut()")
    public void after() {
        ThreadApicodeInfo.removeData();
    }

    @Around("@annotation(authDataPermission)")
    public Object handleAuthDataPermission(ProceedingJoinPoint joinPoint, AuthDataPermission authDataPermission) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String paramName = authDataPermission.paramName();
        MarketingUserDetail user = ThreadContextInfo.getUser();
        if (user == null) {
            return joinPoint.proceed(args);
        }
        boolean isAdmin = user.getRoleList().stream().anyMatch(role -> role.getId() == 1);
        if (isAdmin) {
            return joinPoint.proceed(args);
        }
        List<String> authApiCodes = Splitter.on(",").splitToList(user.getApiCode());
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        int index = getParamIndexByName(method, paramName);
        if (index != -1 && args[index] instanceof List) {
            List<String> argApiCodes = (List<String>)args[index];
            List<String> mixedApiCodes = argApiCodes.stream().filter(authApiCodes::contains).collect(Collectors.toList());
            // 参数apiCode集合为空或参数apiCode集合和权限apiCode集合交集为空，默认为权限apiCode集合
            if (CollectionUtils.isEmpty(argApiCodes) || CollectionUtils.isEmpty(mixedApiCodes)) {
                args[index] = authApiCodes;
            } else {
                args[index] = mixedApiCodes;
            }
        }
        return joinPoint.proceed(args);
    }

    /**
     * 根据参数名获取指定下标
     *
     * @param method 方法
     * @param paramName param名称
     * @return int
     * @author senyang.zheng
     * @date 2024/08/19
     */
    private int getParamIndexByName(Method method, String paramName) {
        String[] paramNames = Arrays.stream(method.getParameters()).map(Parameter::getName).toArray(String[]::new);
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(paramName)) {
                return i;
            }
        }
        return -1;
    }

}
