package com.br.marketing.context.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 中原消金拨打明细上下文
 *
 * @author Guo Zeqiang
 * @dateTime 2023-06-08 18:13
 */
public class ZhongYuanCallRecordCollectDataImpl extends CommonMethodHandlerService {
    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof CallRecordBO) {
            ZhongYuanCallRecordCollectDataImpl.ZhongYuanRuleNecessaryData ruleNecessaryData =
                    new ZhongYuanCallRecordCollectDataImpl.ZhongYuanRuleNecessaryData();
            @SuppressWarnings("unchecked")
            Set<String> set = ((List<CallRecordBO>) transmitFacts).stream()
                    .map(CallRecordBO::getCaseNum).collect(Collectors.toSet());
            ruleNecessaryData.setCustomerMap(customerMarketingSyncUser(set, context.getApiCode()));
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.ZHONGYUAN_CALL_RECORD_DATA_COLLECTION;
    }


    @Data
    public static class ZhongYuanRuleNecessaryData extends RuleNecessaryData {
        /**
         * 上传接口数据
         */
        private Map<String, MarketingSyncUser> customerMap;
    }
}
