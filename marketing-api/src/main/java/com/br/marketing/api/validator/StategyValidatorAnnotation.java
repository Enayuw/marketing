package com.br.marketing.api.validator;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 策略注解校验器
 */
@Documented
@Constraint(validatedBy = {StrategyValidator.class })
@Target({ TYPE })
@Retention(RUNTIME)

public @interface StategyValidatorAnnotation {
    /**
     * 提示信息
     * @return
     */
    String message() default "至少要启用一个模块";

    /**
     * groups
     * @return
     */
    Class<?>[] groups() default { };

    /**
     * payload
     * @return
     */
    Class<? extends Payload>[] payload() default { };
}
