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
        // 设置允许调用java类集合，空集合代表不允许调用
        aviatorEvaluatorInstance.setOption(Options.ALLOWED_CLASS_SET, Collections.emptySet());
        // 设置最多循环次数
        aviatorEvaluatorInstance.setOption(Options.MAX_LOOP_COUNT, 100000);
        // 设置所有浮点数解析为 BigDecimal类型
        aviatorEvaluatorInstance.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
        // 设置缓存aviator表达式
        aviatorEvaluatorInstance.setCachedExpressionByDefault(true);
        // 设置启用LRU缓存和最大容量
        aviatorEvaluatorInstance.useLRUExpressionCache(1000);
        return aviatorEvaluatorInstance;
    }
}
