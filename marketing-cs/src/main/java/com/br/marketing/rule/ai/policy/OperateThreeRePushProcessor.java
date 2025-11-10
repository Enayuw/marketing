package com.br.marketing.rule.ai.policy;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.AiToPolicyRecordMapperBase;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class OperateThreeRePushProcessor extends AbstractBaseAiToPolicy {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    AiToPolicyRecordMapperBase aiToPolicyRecordMapperBase;

    @Override
    public String getOperationType() {
        return "3_RE";
    }

    @Override
    public String generateBatchNumber(MarketingSyncUser syncUser) {
        String apiCode = syncUser.getApiCode();
        String appletDate = syncUser.getAppletDate().replace("-", "");
        String reserveField1 = syncUser.getReserveField1();
        JSONObject jsonObject = JSONObject.parseObject(reserveField1);
        String rePushCount = jsonObject.get("rePushNum").toString();
        List<String> apiCodeOfpushPolicy = marketingCommonConfig.getApiCodeOfpushPolicy();

        if (ObjectUtil.isNotEmpty(apiCodeOfpushPolicy) && apiCodeOfpushPolicy.contains(apiCode)) {
            return ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                    ? (appletDate + jsonObject.getString("batchNumber"))+ "_RE_" + rePushCount
                    : (appletDate + "_" + apiCode)+ "_RE_" + rePushCount;
        } else {
            return ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                    ? jsonObject.getString("batchNumber")+ "_RE_" + rePushCount
                    : (appletDate + "_" + apiCode)+ "_RE_" + rePushCount;
        }
    }

}
