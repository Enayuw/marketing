package com.br.marketing.rule.ai.policy;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.AiToPolicyRecordMapperBase;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
public class OperateFourRePushProcessor extends OperateFourProcessor {

    @Override
    public String getOperationType() {
        return "4_RE";
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
