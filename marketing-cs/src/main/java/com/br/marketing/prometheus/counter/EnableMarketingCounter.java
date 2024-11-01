package com.br.marketing.prometheus.counter;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author zhen.li1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Import({MarketingCounter.class})
@Documented
public @interface  EnableMarketingCounter {

    /**
     * 命名空间，此处设置的命名空间将作为MarketingCounter的统计方法的前缀名
     * @return
     */
    String namespace();
}
