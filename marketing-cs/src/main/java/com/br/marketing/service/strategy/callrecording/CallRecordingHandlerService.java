package com.br.marketing.service.strategy.callrecording;

import com.br.marketing.mapper.CallRecordConfigMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CallRecordingHandlerService {

    @Resource
    private CallRecordConfigMapper callRecordConfigMapper;

    /**
     * 客户规则缓存
     */
    private static LoadingCache<String, String> ruleCache = null;

    @PostConstruct
    private void init() {
        ruleCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats()
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) {
                        String result = callRecordConfigMapper.customerRuleLabels(key);
                        return Objects.requireNonNullElse(result, "");
                    }
                });
    }

    /**
     * 获取客户规则
     */
    public static void invalidateAll() {
        if (ruleCache != null) {
            log.warn("客户规则清理...");
            ruleCache.invalidateAll();
        }
    }

    /**
     * 获取客户规则
     * @return 客户规则列表
     */
    public String customerRules(String apiCode) {
        try {
            return ruleCache.get(apiCode);
        } catch (ExecutionException e) {
            log.error("获取客户规则失败, ", e);
        }
        return "";
    }

}
