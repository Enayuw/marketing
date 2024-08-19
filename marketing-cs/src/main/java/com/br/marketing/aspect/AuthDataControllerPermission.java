package com.br.marketing.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 营销后台用户apiCode权限过滤注解，仅可以在Controller中使用，支持多个apiCode，不支持去重（去重逻辑自实现）。
 *
 * @author senyang.zheng
 * @date 2024/08/19
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AuthDataControllerPermission {
    String paramName() default "apiCodes";
}
