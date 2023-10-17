package com.br.marketing.aspect;

import com.br.marketing.enums.ValidityPeriodResendEnum;
import org.springframework.stereotype.Service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 有效期变更重推类型
 *
 * @author senyang.zheng
 * @date 2023/10/08
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Service
public @interface ValidityPeriodResendType {
    ValidityPeriodResendEnum resendType();
}
