package com.br.marketing.rule.ai.policy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.AiToPolicyRecordMapperBase;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
@Slf4j
public class OperateFiveRePushProcessor extends AbstractBaseAiToPolicy {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    AiToPolicyRecordMapperBase aiToPolicyRecordMapperBase;

    @Autowired
    RedisChgService redisChgService;

    @Override
    public String getOperationType() {
        return "5_RE";
    }

    @Override
    public String generateBatchNumber(MarketingSyncUser syncUser) {

        String apiCode = syncUser.getApiCode();
        String operateType = syncUser.getOperateType();
        String userType = syncUser.getUserType();
        String nowDate = LocalDate.now().toString().replace("-", "");

        JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
        String rePeatNum = jsonObject.get("rePeatNum").toString();
        String rePushCount = jsonObject.get("rePushNum").toString();
        return nowDate + "_" + apiCode + "_" + operateType + "_" + userType + "_" + rePeatNum + "_RE_" + rePushCount;
    }


}
