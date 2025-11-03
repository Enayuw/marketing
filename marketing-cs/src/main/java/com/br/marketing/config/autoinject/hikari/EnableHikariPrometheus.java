package com.br.marketing.config.autoinject.hikari;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(HikariPrometheusConfiguration.class)
@Documented
public @interface EnableHikariPrometheus {
}
