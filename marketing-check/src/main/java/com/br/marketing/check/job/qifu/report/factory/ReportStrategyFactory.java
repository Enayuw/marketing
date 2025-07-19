package com.br.marketing.check.job.qifu.report.factory;

import com.br.marketing.check.job.qifu.report.strategy.ReportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ClassName ReportStrategyFactory
 * @Author hang.zhou
 * @Date 2025/7/18
 */
@Component
@SuppressWarnings("rawtypes")
public class ReportStrategyFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReportStrategyFactory.class);

    @Autowired
    private Map<String, ReportStrategy> strategyMap;

    public ReportStrategy getStrategy(String reportType) {
        if (strategyMap == null || strategyMap.isEmpty()) {
            logger.warn("strategyMap is null or empty! reportType: {}", reportType);
            return null;
        }
        ReportStrategy strategy = strategyMap.get(reportType);
        if (strategy == null) {
            logger.warn("Strategy not found for reportType: {} , available keys: {}", reportType, strategyMap.keySet());
        }
        return strategy;
    }

}
