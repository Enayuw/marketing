package com.br.marketing.context.impl;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 你我贷上下文处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023/3/23 23:20
 */
@Service
@Slf4j
public class WeiJuRuleCollectDataImpl extends CommonMethodHandlerService {

    @Override
    @SuppressWarnings("unchecked")
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            WeiJuRuleNecessaryData data = new WeiJuRuleNecessaryData();
            context.setRuleNecessaryData(data);
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Map<String, SyncUserValidityPeriodsBO> boMap = newCustomerSyncUserValidityPeriod(transferList, context.getApiCode());
            data.setSyncUserValidityPeriodMap(boMap);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.WEIJU_TRANSFER_FILTER_COLLECTION;
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class WeiJuRuleNecessaryData extends RuleNecessaryData {
        /**
         * 状态
         */
        private String inversionStatus;


        private Map<String, SyncUserValidityPeriodsBO> syncUserValidityPeriodMap;

    }

}
