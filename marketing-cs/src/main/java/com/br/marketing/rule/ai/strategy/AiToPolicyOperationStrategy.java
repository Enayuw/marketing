package com.br.marketing.rule.ai.strategy;

import com.br.marketing.entity.MarketingSyncUser;

/**
 * AI推决策操作策略接口
 * 定义所有操作类型策略必须实现的方法
 * 
 * @author AI Assistant
 * @date 2024
 */
public interface AiToPolicyOperationStrategy {

    /**
     * 获取操作类型
     */
    String getOperationType();

    /**
     * 生成批次号
     */
    String generateBatchNumber(MarketingSyncUser syncUser);

    /**
     * 插入去重记录
//     */
//    boolean insertRecord(MarketingSyncUser syncUser);

}