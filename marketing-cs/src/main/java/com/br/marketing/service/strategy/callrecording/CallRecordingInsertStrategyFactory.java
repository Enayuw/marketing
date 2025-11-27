package com.br.marketing.service.strategy.callrecording;

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

    @Resource
    private CallRecordingHandlerService callRecordingHandlerService;

    private final Map<String, CallRecordingInsertStrategy> strategyNameMap = new ConcurrentHashMap<>();

    /**
     * 默认的客户规则key
     */

    @PostConstruct
    public void init() {
        // 初始化策略缓存
        if (strategies == null || strategies.isEmpty()) {
            log.warn("未找到任何CallRecordingInsertStrategy策略实现");
            return;
        }
        // 创建策略类名到策略实例的映射
        for (CallRecordingInsertStrategy strategy : strategies) {
            String simpleClassName = strategy.getClass().getSimpleName();
            strategyNameMap.put(simpleClassName, strategy);
            log.warn("注册策略: {} -> {}", simpleClassName, strategy.getClass().getName());
        }
    }

    /**
     * 根据apiCode获取对应的策略
     *
     * @param apiCode API编码
     * @return 策略实例，如果找不到则返回默认策略
     */
    public CallRecordingInsertStrategy getStrategy(String apiCode) {
        String name = callRecordingHandlerService.customerRules(apiCode);
        return strategyNameMap.get(name);
    }
}

