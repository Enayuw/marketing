package com.br.marketing.rule.ai.policy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI推决策策略工厂
 * 管理和获取所有操作类型策略
 */
@Component
@Slf4j
public class AiToPolicyProcessorFactory {
    
    private final Map<String, AiToPolicyProcessor> strategyMap;
    
    @Autowired
    public AiToPolicyProcessorFactory(List<AiToPolicyProcessor> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                    AiToPolicyProcessor::getOperationType,
                    Function.identity(),
                    (existing, replacement) -> {
                        throw new IllegalStateException("重复的操作类型策略: " + existing.getOperationType());
                    }
                ));
    }

    /**
     * 根据操作类型获取策略
     */
    public AiToPolicyProcessor getStrategy(String operationType) {
        AiToPolicyProcessor strategy = strategyMap.get(operationType);
        if (strategy == null) {
            throw new IllegalArgumentException("speed中配置了未知的操作类型: " + operationType);
        }
        return strategy;
    }
}