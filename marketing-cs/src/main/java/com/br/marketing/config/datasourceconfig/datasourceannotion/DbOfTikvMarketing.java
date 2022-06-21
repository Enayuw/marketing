package com.br.marketing.config.datasourceconfig.datasourceannotion;

import java.lang.annotation.*;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DbOfTikvMarketing {
}
