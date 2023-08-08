package com.br.marketing.context.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhen.Li
 * @version 1.0
 * @date 2023/08/01 16:45
 * 众邦规则所需要的数据
 */
@Service
public class ZhongBangRuleCollectDataImpl extends CommonMethodHandlerService {


    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty()) {
            ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData ruleNecessaryData = new ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData();
            if (transmitFacts.get(0) instanceof CallRecordBO) {
                Set<String> set = ((List<CallRecordBO>) transmitFacts).stream()
                        .map(CallRecordBO::getCaseNum).collect(Collectors.toSet());
                ruleNecessaryData.setCallRecordCustomerMap(customerMarketingSyncUser(set, context.getApiCode()));
            }
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.ZHONGBANG_DATA_COLLECTION;
    }


    @Data
    public class ZhongBangRuleNecessaryData extends RuleNecessaryData {
        /**
         * 拨打明细原始数据
         */
        private Map<String, MarketingSyncUser> callRecordCustomerMap;

    }


}
