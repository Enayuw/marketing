package com.br.marketing.context.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 桔子转化上下文处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023/3/15 9:45
 */
@Service
@Slf4j
public class OrangeCollectDataImpl extends CommonMethodHandlerService {

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            OrangeRuleNecessaryData data = new OrangeRuleNecessaryData();
            context.setRuleNecessaryData(data);
            Set<String> set = ((List<MarketingTransferSyncUser>) transmitFacts)
                    .stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, MarketingSyncUser> map = customerMarketingSyncUser(set, context.getApiCode());
            data.setCustomerMap(map);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.ORANGE_DATA_COLLECTION;
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class OrangeRuleNecessaryData extends RuleNecessaryData {
        /**
         * 原始数据
         */
        private Map<String, MarketingSyncUser> customerMap;

        /**
         * 2023-03-14 18:23
         * 生效截止时间 格式yyyy-mm-dd HH:mm:ss
         */
        private String expireDate;
    }


}
