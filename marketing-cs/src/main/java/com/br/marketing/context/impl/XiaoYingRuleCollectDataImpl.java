package com.br.marketing.context.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 小赢上下文处理
 *
 * @author Guo Zeqiang
 * @dateTime 2022/5/27 10:00
 */
@Service
public class XiaoYingRuleCollectDataImpl extends CommonMethodHandlerService {

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            XiaoYingRuleNecessaryData ruleNecessaryData = new XiaoYingRuleNecessaryData();
            @SuppressWarnings("unchecked")
            Set<String> set = ((List<MarketingTransferSyncUser>) transmitFacts).stream()
                    .map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            ruleNecessaryData.setCustomerMap(customerMarketingSyncUser(set, context.getApiCode()));
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.XIAO_YING_RULE_DATA_COLLECTION;
    }


    @Data
    public static class XiaoYingRuleNecessaryData extends RuleNecessaryData {
        /**
         * 上传接口数据
         */
        private Map<String, MarketingSyncUser> customerMap;
    }
}
