package com.br.marketing.rule.ai.policy;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * 操作类型4策略实现
 * 继承AbstractBaseAiToPolicy，实现AiToPolicyProcessor
 * 重写batchNumber生成、字段映射逻辑
 */
@Component
@Slf4j
public class OperateFourProcessor extends AbstractBaseAiToPolicy {
    @Override
    public String getOperationType() {
        return "4";
    }

    @Override
    public String generateBatchNumber(MarketingSyncUser syncUser) {
        return generateBaseBatchNumber(syncUser);
    }

    /**
     * 生成基础批次号（供子类复用）
     * @param syncUser 同步用户信息
     * @return batchNumber
     */
    protected String generateBaseBatchNumber(MarketingSyncUser syncUser) {
        String apiCode = syncUser.getApiCode();
        String appletDate = syncUser.getAppletDate().replace("-", "");
        String reserveField1 = syncUser.getReserveField1();
        JSONObject jsonObject = JSONObject.parseObject(reserveField1);

        String userType = syncUser.getUserType();
        return ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                ? jsonObject.getString("batchNumber")
                : (appletDate + "_" + apiCode + "_" + userType);
    }

    @Override
    protected void executeFieldMapping(ProcessHandlerContext context, JSONObject jsonObject) {
        HashMap<String, JSONObject> fieldKeyMapping = marketingCommonConfig.getFieldKeyMapping();
        JSONObject mapping = fieldKeyMapping.get(context.getApiCode());
        if (ObjectUtil.isNotEmpty(mapping)) {
            for (String s : mapping.keySet()) {
                String toKey = mapping.getString(s);
                String oldV = jsonObject.getString(toKey);
                String newV = jsonObject.getString(s);
                if (StringUtils.isBlank(oldV) && StringUtils.isNotBlank(newV)) {
                    jsonObject.put(toKey, newV);
                }
            }
        }
    }
}