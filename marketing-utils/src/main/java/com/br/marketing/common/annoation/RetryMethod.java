package com.br.marketing.common.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 重试注解
 */
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RetryMethod {
    /**
     * 间隔时间后 重试最大次数
     * @return
     */
    int retryNum() default 3;

    /**
     * 立即重试最大次数
     * @return
     */
    int retryNowNum() default 0;

    /**
     * 是否db重试
     * 如开启db重试 需要注意以下几点
     * 1、参数必然有两个参数，其第二个参数为Integer类型并且1为该参数的重试程序占位符，调用该方法的时候禁止传1
     * 2、返回结果必然是Result<T>对象
     * 3、返回结果 result code值为500或者异常 才会进入db重试
     * @return
     */
    boolean isOrNoDbRetry() default false;
}
