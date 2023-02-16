package com.br.marketing.common.annoation;

import com.br.marketing.common.enums.DistributeTypeEnum;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 重试注解
 */
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeLog {
}
