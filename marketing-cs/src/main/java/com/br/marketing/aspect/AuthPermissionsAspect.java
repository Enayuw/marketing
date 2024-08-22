package com.br.marketing.aspect;

import java.util.List;
import java.util.stream.Collectors;

import com.github.pagehelper.util.StringUtil;
import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.context.ThreadApicodeInfo;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.vo.BaseAuthPermissionData;
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

    @Around("@annotation(authDataControllerPermission)")
    public Object handleAuthDataPermission(ProceedingJoinPoint joinPoint, AuthDataControllerPermission authDataControllerPermission)
        throws Throwable {
        Object[] args = joinPoint.getArgs();
        MarketingUserDetail user = ThreadContextInfo.getUser();
        if (user == null) {
            return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_USER_INVALID_SESSION_ERROR);
        }
        boolean isAdmin = user.getRoleList().stream().anyMatch(role -> role.getId() == 1);
        if (isAdmin) {
            return joinPoint.proceed(args);
        }
        if(StringUtil.isEmpty(user.getApiCode())){
            return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_USER_API_CODE_ERROR);
        }
        List<String> authApiCodes = Splitter.on(",").splitToList(user.getApiCode());
        // 处理封装Object类型的参数
        List<String> mixedApiCodes;
        if (args[0] instanceof BaseAuthPermissionData) {
            BaseAuthPermissionData baseAuthPermissionData = (BaseAuthPermissionData)args[0];
            List<String> argApiCodes = baseAuthPermissionData.getApiCodes();
            if (CollectionUtils.isEmpty(argApiCodes)) {
                baseAuthPermissionData.setApiCodes(authApiCodes);
                return joinPoint.proceed(args);
            } else {
                mixedApiCodes = argApiCodes.stream().filter(authApiCodes::contains).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(mixedApiCodes)) {
                    return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_USER_API_CODE_ERROR);
                }
                baseAuthPermissionData.setApiCodes(mixedApiCodes);
                return joinPoint.proceed(args);
            }
        }
        // 处理散装参数
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        String[] parameterNames = methodSignature.getParameterNames();
        int index = ArrayUtils.indexOf(parameterNames, authDataControllerPermission.paramName());
        if (index != -1 && args[index] == null) {
            args[index] = authApiCodes;
            return joinPoint.proceed(args);
        }
        if (index != -1 && args[index] instanceof List) {
            List<String> argApiCodes = (List<String>)args[index];
            if (CollectionUtils.isEmpty(argApiCodes)) {
                args[index] = authApiCodes;
                return joinPoint.proceed(args);
            } else {
                mixedApiCodes = argApiCodes.stream().filter(authApiCodes::contains).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(mixedApiCodes)) {
                    return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_USER_API_CODE_ERROR);
                }
                args[index] = mixedApiCodes;
            }
        }
        return joinPoint.proceed(args);
    }

}
