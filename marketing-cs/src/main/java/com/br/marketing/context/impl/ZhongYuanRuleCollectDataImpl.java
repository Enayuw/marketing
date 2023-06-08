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
 * @author zhen.Li
 * @version 1.0
 * @date 2023/06/08 19:45
 * 中原规则所需要的数据
 */
@Service
public class ZhongYuanRuleCollectDataImpl extends CommonMethodHandlerService {


    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData ruleNecessaryData = new ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, MarketingSyncUser> collect = customerMarketingSyncUser(set, context.getApiCode());
            ruleNecessaryData.setCustomerMap(collect);
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.HAI_ER_RULE_DATA_COLLECTION;
    }


    @Data
    public class ZhongYuanRuleNecessaryData extends RuleNecessaryData {
        /**
         * 客户上传信息
         */
        private Map<String, MarketingSyncUser> customerMap;
    }


}
