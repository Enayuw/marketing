package com.br.marketing.aspect;

import com.br.marketing.enums.MqIdempotentTableType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MQ消息幂等性注解
 * 用于标记需要幂等性处理的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MqIdempotent {
    /**
     * 使用的幂等表类型，默认为通用表（COMMON）
     */
    MqIdempotentTableType tableType() default MqIdempotentTableType.COMMON;
    
    /**
     * 幂等键字段名，默认为从MqFact中获取idempotentKey
     */
    String idempotentKeyField() default "idempotentKey";
}

