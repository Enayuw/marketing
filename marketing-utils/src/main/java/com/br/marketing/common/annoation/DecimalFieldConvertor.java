package com.br.marketing.common.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.RoundingMode;

@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DecimalFieldConvertor {

    int scale() default 3;

    /**
     * 小数保留方式
     */
    RoundingMode roundingMode() default RoundingMode.HALF_UP;

    boolean isPercent() default true;
}
