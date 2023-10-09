package com.br.marketing.config.autoinject.druid;

import com.br.marketing.config.autoinject.druid.DruidRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(DruidRegister.class)
@Documented
public @interface EnableDruidPrometheus {
}
