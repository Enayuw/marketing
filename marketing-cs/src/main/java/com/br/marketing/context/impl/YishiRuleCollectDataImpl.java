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
 * 医时上下文处理
 *
 * @author kongbx
 */
@Service
@Slf4j
public class YishiRuleCollectDataImpl extends CommonMethodHandlerService {

    @Override
    @SuppressWarnings("unchecked")
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            YishiRuleNecessaryData data = new YishiRuleNecessaryData();
            context.setRuleNecessaryData(data);
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Map<String, SyncUserValidityPeriodsBO> boMap = newCustomerSyncUserValidityPeriod(transferList, context.getApiCode());
            data.setSyncUserValidityPeriodMap(boMap);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.YISHI_TRANSFER_FILTER_COLLECTION;
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class YishiRuleNecessaryData extends RuleNecessaryData {
        /**
         * 状态
         */
        private String inversionStatus;


        private Map<String, SyncUserValidityPeriodsBO> syncUserValidityPeriodMap;

    }

}
