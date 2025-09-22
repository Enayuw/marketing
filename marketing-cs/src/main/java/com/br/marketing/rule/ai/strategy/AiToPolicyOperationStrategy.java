//package com.br.marketing.rule.ai.strategy;
//
//import com.alibaba.fastjson.JSONObject;
//import com.br.marketing.entity.MarketingSyncUser;
//
///**
// * AI推决策操作策略接口
// * 封装不同操作类型的差异化逻辑
// */
//public interface AiToPolicyOperationStrategy {
//
//    /**
//     * 获取操作类型
//     */
//    String getOperationType();
//
//    /**
//     * 获取批次号
//     */
//    String getBatchNumber(MarketingSyncUser syncUser, String appletDate, String apiCode,
//                         String userType, JSONObject jsonObject);
//
//    /**
//     * 是否需要Redis锁
//     */
//    boolean needsRedisLock();
//
//    /**
//     * 生成Redis锁key
//     */
//    String generateRedisKey(String yyyyMMdd, String apiCode, String userType, MarketingSyncUser syncUser, String ruleLabel);
//
//    /**
//     * 获取去重字段值
//     */
//    String getDeduplicationValue(MarketingSyncUser syncUser);
//
//    /**
//     * 生成动态批次号
//     */
//    String generateDynamicBatchNumber(String yyyyMMdd, String apiCode, String operationType, String userType, int pushCount);
//
//    /**
//     * 处理特殊逻辑
//     */
//    void handleSpecialLogic(MarketingSyncUser syncUser) throws RuntimeException;
//}