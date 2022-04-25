package com.br.marketing.config.datasourceconfig.datasourceannotion;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DbOfTikvMarketing {
}
