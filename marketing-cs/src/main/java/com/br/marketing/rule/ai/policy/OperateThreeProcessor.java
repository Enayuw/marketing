package com.br.marketing.rule.ai.policy;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingSyncUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 操作类型3策略实现
 * 继承AbstractBaseAiToPolicy，实现AiToPolicyProcessor
 * 重写batchNumber生成
 */
@Component
@Slf4j
public class OperateThreeProcessor extends AbstractBaseAiToPolicy {
    @Override
    public String getOperationType() {
        return "3";
    }

    @Override
    public String generateBatchNumber(MarketingSyncUser syncUser) {
        return generateBaseBatchNumber(syncUser);
    }

    /**
     * 生成基础批次号（供子类复用）
     *
     * @param syncUser 同步用户信息
     * @return 基础批次号
     */
    protected String generateBaseBatchNumber(MarketingSyncUser syncUser) {
        String apiCode = syncUser.getApiCode();
        String appletDate = syncUser.getAppletDate().replace("-", "");
        String reserveField1 = syncUser.getReserveField1();
        JSONObject jsonObject = JSONObject.parseObject(reserveField1);

        List<String> apiCodeOfpushPolicy = marketingCommonConfig.getApiCodeOfpushPolicy();

        if (ObjectUtil.isNotEmpty(apiCodeOfpushPolicy) && apiCodeOfpushPolicy.contains(apiCode)) {
            return ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                    ? (appletDate + jsonObject.getString("batchNumber"))
                    : (appletDate + "_" + apiCode);
        } else {
            return ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                    ? jsonObject.getString("batchNumber")
                    : (appletDate + "_" + apiCode);
        }
    }
}