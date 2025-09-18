package com.br.marketing.rule.ai.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.ai.strategy.AiToPolicyOperationStrategy;
import com.br.marketing.rule.common.CommonRuleLabelEnum;
import org.springframework.stereotype.Component;

/**
 * 操作类型4的策略实现
 */
@Component
public class OperationTypeFourStrategy implements AiToPolicyOperationStrategy {
    
    @Override
    public String getOperationType() {
        return "4";
    }
    
    @Override
    public String getBatchNumber(MarketingSyncUser syncUser, String appletDate, String apiCode, 
                               String userType, JSONObject jsonObject) {
        return ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                ? jsonObject.getString("batchNumber")
                : (appletDate + "_" + apiCode + "_" + userType);
    }
    
    @Override
    public boolean needsRedisLock() {
        return false;
    }
    
    @Override
    public String generateRedisKey(String yyyyMMdd, String apiCode, String userType, MarketingSyncUser syncUser, String ruleLabel) {
        return null; // 不需要Redis锁
    }
    
    @Override
    public String getDeduplicationValue(MarketingSyncUser syncUser) {
        return syncUser.getCustNum();
    }
    
    @Override
    public String generateDynamicBatchNumber(String yyyyMMdd, String apiCode, String operationType, String userType, int pushCount) {
        return null; // 操作类型4不需要动态批次号
    }
    
    @Override
    public void handleSpecialLogic(MarketingSyncUser syncUser) throws RuntimeException {
        // 无特殊逻辑
    }
}