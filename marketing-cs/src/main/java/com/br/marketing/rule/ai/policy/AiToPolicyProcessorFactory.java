package com.br.marketing.rule.ai.policy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI推决策策略工厂
 * 管理和验证所有操作类型策略
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
//
//    @PostConstruct
//    public void validateStrategies() {
//        Set<String> expectedTypes = Set.of("4", "5", "6"); // 可以从配置文件读取
//        Set<String> actualTypes = strategyMap.keySet();
//
//        expectedTypes.stream()
//                .filter(type -> !actualTypes.contains(type))
//                .forEach(type -> {
//                    throw new IllegalStateException("缺少操作类型 " + type + " 的策略实现");
//                });
//
//        log.warn("AI推决策策略验证完成，支持的操作类型: {}", actualTypes);
//    }
    
    /**
     * 根据操作类型获取策略
     */
    public AiToPolicyProcessor getStrategy(String operationType) {
        AiToPolicyProcessor strategy = strategyMap.get(operationType);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的操作类型: " + operationType);
        }
        return strategy;
    }
    
    /**
     * 检查是否支持指定的操作类型
     */
    public boolean isSupported(String operationType) {
        return strategyMap.containsKey(operationType);
    }
    
    /**
     * 获取所有支持的操作类型
     */
    public Set<String> getSupportedOperationTypes() {
        return strategyMap.keySet();
    }
}