package com.br.marketing.service.strategy.callrecording;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    @PostConstruct
    public void init() {
        // 初始化策略缓存
        if (strategies != null) {
            for (CallRecordingInsertStrategy strategy : strategies) {
                strategy.getApiCodes().forEach((String apiCode) -> strategyCache.put(apiCode, strategy));
            }
        }
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

