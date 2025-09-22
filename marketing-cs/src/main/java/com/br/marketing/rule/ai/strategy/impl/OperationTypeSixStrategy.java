//package com.br.marketing.rule.ai.strategy.impl;
//
//import com.alibaba.fastjson.JSONObject;
//import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
//import com.br.marketing.entity.MarketingSyncUser;
//import com.br.marketing.rule.ai.strategy.AiToPolicyOperationStrategy;
//import com.br.marketing.rule.common.CommonRuleLabelEnum;
//import org.springframework.stereotype.Component;
//
///**
// * 操作类型6的策略实现
// */
//@Component
//public class OperationTypeSixStrategy implements AiToPolicyOperationStrategy {
//
//    @Override
//    public String getOperationType() {
//        return "6";
//    }
//
//    @Override
//    public String getBatchNumber(MarketingSyncUser syncUser, String appletDate, String apiCode,
//                               String userType, JSONObject jsonObject) {
//        return syncUser.getReserveField2();
//    }
//
//    @Override
//    public boolean needsRedisLock() {
//        return true;
//    }
//
//    @Override
//    public String generateRedisKey(String yyyyMMdd, String apiCode, String userType, MarketingSyncUser syncUser, String ruleLabel) {
//        return RedisKeyConstant.AI_TOPOLICY_PUSH_COUNTER.concat(
//            String.format("%s:%s:%s:%s:%s", yyyyMMdd, apiCode, userType, ruleLabel, syncUser.getCell())
//        );
//    }
//
//    @Override
//    public String getDeduplicationValue(MarketingSyncUser syncUser) {
//        return syncUser.getCell();
//    }
//
//    @Override
//    public String generateDynamicBatchNumber(String yyyyMMdd, String apiCode, String operationType, String userType, int pushCount) {
//        return yyyyMMdd + "-" + apiCode + "-" + operationType + "-" + userType + "-" + pushCount;
//    }
//
//    @Override
//    public void handleSpecialLogic(MarketingSyncUser syncUser) throws RuntimeException {
//        // 无特殊逻辑
//    }
//}