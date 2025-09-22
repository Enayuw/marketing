//package com.br.marketing.rule.ai.strategy.impl;
//
//import com.alibaba.fastjson.JSONObject;
//import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
//import com.br.marketing.entity.MarketingSyncUser;
//import com.br.marketing.rule.ai.strategy.AiToPolicyOperationStrategy;
//import com.br.marketing.rule.common.CommonRuleLabelEnum;
//import com.br.marketing.speedconfig.MarketingCommonConfig;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
///**
// * 操作类型5的策略实现
// */
//@Component
//public class OperationTypeFiveStrategy implements AiToPolicyOperationStrategy {
//
//    @Autowired
//    private MarketingCommonConfig marketingCommonConfig;
//
//    @Override
//    public String getOperationType() {
//        return "5";
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
//            String.format("%s:%s:%s:%s:%s", yyyyMMdd, apiCode, userType, ruleLabel, syncUser.getCustNum())
//        );
//    }
//
//    @Override
//    public String getDeduplicationValue(MarketingSyncUser syncUser) {
//        return syncUser.getCustNum();
//    }
//
//    @Override
//    public String generateDynamicBatchNumber(String yyyyMMdd, String apiCode, String operationType, String userType, int pushCount) {
//        return yyyyMMdd + "-" + apiCode + "-" + operationType + "-" + userType + "-" + pushCount;
//    }
//
//    @Override
//    public void handleSpecialLogic(MarketingSyncUser syncUser) throws RuntimeException {
//        if (marketingCommonConfig.getXieChengActivateRabbitMqSwitch()) {
//            throw new RuntimeException("模拟redis异常");
//        }
//    }
//}