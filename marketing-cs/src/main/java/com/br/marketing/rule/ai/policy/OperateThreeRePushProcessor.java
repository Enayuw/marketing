package com.br.marketing.rule.ai.policy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingSyncUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class OperateThreeRePushProcessor extends OperateThreeProcessor {




    @Override
    public String getOperationType() {
        return "3_RE";
    }

    @Override
    public String generateBatchNumber(MarketingSyncUser syncUser) {
        String baseBatchNumber = generateBaseBatchNumber(syncUser);
        // 获取重推次数
        String reserveField1 = syncUser.getReserveField1();
        JSONObject jsonObject = JSONObject.parseObject(reserveField1);
        String rePushCount = jsonObject.get("rePushNum").toString();

        // 在基础批次号后添加重推标识
        return baseBatchNumber + "_RE_" + rePushCount;
    }

}
