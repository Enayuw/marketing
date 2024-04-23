package com.br.marketing.aspect;

import com.br.marketing.enums.InterfaceOperationsEnum;

import java.lang.annotation.*;

/**
 * @author kongbx
 * @date 2024/4/17
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogRecordAnnotation {

    /**
     * 关联的接口操作枚举
     * 对应：InterfaceOperationsEnum
     */
    InterfaceOperationsEnum bizNo();

    /**
     * 比较详细的一条操作日志 比如：
     * 修改了数据包一中的原开启撞库时间4月15号 20:24:34 的设定撞得量级2,000,000修改为4月16号 20:24:34的设定撞得量级1,000,000
     */
    String extendInfo() default "";

    /**
     * 修改前的原值(新增数据不需要填)
     */
    String originalValue() default "";

}
