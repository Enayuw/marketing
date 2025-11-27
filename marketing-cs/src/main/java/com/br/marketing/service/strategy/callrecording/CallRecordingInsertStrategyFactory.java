package com.br.marketing.service.strategy.callrecording;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CallRecording插入策略工厂
 *
 * @author kongbx
 * @date 2025/11/26
 */
@Slf4j
@Component
public class CallRecordingInsertStrategyFactory {

    @Autowired
    private List<CallRecordingInsertStrategy> strategies;

    /**
     * 策略缓存，key为apiCode，value为策略实例
     */
    private final Map<String, CallRecordingInsertStrategy> strategyCache = new ConcurrentHashMap<>();

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @PostConstruct
    public void init() {
        // 初始化策略缓存
        if (strategies == null || strategies.isEmpty()) {
            log.warn("未找到任何CallRecordingInsertStrategy策略实现");
            return;
        }

        Map<String, String> generalCallbackConfig = marketingCommonConfig.getGeneralCallbackConfig();
        if (generalCallbackConfig == null || generalCallbackConfig.isEmpty()) {
            log.warn("generalCallbackConfig配置为空，无法初始化策略映射");
            return;
        }

        // 创建策略类名到策略实例的映射
        Map<String, CallRecordingInsertStrategy> strategyNameMap = new ConcurrentHashMap<>();
        for (CallRecordingInsertStrategy strategy : strategies) {
            String simpleClassName = strategy.getClass().getSimpleName();
            strategyNameMap.put(simpleClassName, strategy);
            log.warn("注册策略: {} -> {}", simpleClassName, strategy.getClass().getName());
        }

        // 根据配置建立apiCode到策略的映射
        for (Map.Entry<String, String> entry : generalCallbackConfig.entrySet()) {
            String apiCode = entry.getKey();
            String strategyClassName = entry.getValue();

            CallRecordingInsertStrategy strategy = strategyNameMap.get(strategyClassName);
            if (strategy == null) {
                log.warn("未找到策略实现类: {}，apiCode: {} 将无法使用", strategyClassName, apiCode);
                continue;
            }

            strategyCache.put(apiCode, strategy);
            log.warn("配置策略映射: apiCode={} -> strategy={}", apiCode, strategyClassName);
        }

        log.warn("策略工厂初始化完成，共配置 {} 个策略映射", strategyCache.size());
    }

    /**
     * 根据apiCode获取对应的策略
     *
     * @param apiCode API编码
     * @return 策略实例，如果找不到则返回默认策略
     */
    public CallRecordingInsertStrategy getStrategy(String apiCode) {
        return strategyCache.get(apiCode);
    }
}

