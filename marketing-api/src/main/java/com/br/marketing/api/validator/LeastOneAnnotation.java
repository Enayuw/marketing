package com.br.marketing.api.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * LeastOneAnnotation
 */
@Documented
@Constraint(validatedBy = {LeastOneValidator.class})
@Target({ FIELD })
@Retention(RUNTIME)
public @interface LeastOneAnnotation {
    /**
     * 每个模块要选择至少一个元素
     * @return String
     */
    String message() default "每个模块要选择至少一个元素";

    /**
     * groups
     * @return Class<?>[]
     */
    Class<?>[] groups() default { };

    /**
     * payload
     * @return Class<? extends Payload>[]
     */
    Class<? extends Payload>[] payload() default { };
}
