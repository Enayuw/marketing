package com.br.marketing.rule.ai.policy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingSyncUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
@Component
@Slf4j
public class OperateSixRePushProcessor  extends OperateSixProcessor {

    @Override
    public String getOperationType() {
        return "6_RE";
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

    public boolean insertRecord(MarketingSyncUser syncUser) {
        return Boolean.TRUE;
    }
}
