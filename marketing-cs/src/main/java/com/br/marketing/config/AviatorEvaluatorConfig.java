package com.br.marketing.config;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Options;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * 描述：AviatorEvaluator配置类
 *
 * @author junzhe.ma
 * @date 2026-01-08 10:12
 */
@Configuration
public class AviatorEvaluatorConfig {

    @Bean("cleanRuleAviatorEvaluatorInstance")
    public AviatorEvaluatorInstance aviatorEvaluatorInstance() {
        AviatorEvaluatorInstance aviatorEvaluatorInstance = AviatorEvaluator.newInstance();
        aviatorEvaluatorInstance.setOption(Options.ALLOWED_CLASS_SET, Collections.emptySet());
        aviatorEvaluatorInstance.setOption(Options.MAX_LOOP_COUNT, 100000);
        return aviatorEvaluatorInstance;
    }
}
