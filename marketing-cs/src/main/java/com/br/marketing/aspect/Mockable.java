package com.br.marketing.aspect;

import java.lang.annotation.*;

/**
 * @ClassName Mockable
 * @Author kongbx
 * @Date 2025/6/27 10:55
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mockable {

    /**
     * mock名称
     */
    String mockName() default "";

}
