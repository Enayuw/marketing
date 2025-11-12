package com.br.marketing.rule.ai.policy;

import com.br.marketing.entity.MarketingSyncUser;

/**
 * AI推决策操作策略接口
 * 定义所有操作类型策略必须实现的方法
 */
public interface AiToPolicyProcessor {

    /**
     * 获取操作类型
     */
    String getOperationType();

    /**
     * 生成批次号
     */
    String generateBatchNumber(MarketingSyncUser syncUser);
}