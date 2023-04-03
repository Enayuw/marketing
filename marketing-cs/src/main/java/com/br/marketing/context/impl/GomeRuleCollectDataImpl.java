package com.br.marketing.context.impl;

import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/4/3 10:45
 * 国美规则所需要的数据
 */
@Service
public class GomeRuleCollectDataImpl extends CommonMethodHandlerService{

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            GomeRuleCollectDataImpl.GomeRuleNecessaryData ruleNecessaryData = new GomeRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            ruleNecessaryData.setSyncUserValidityPeriodMap(customerSyncUserValidityPeriod(transferList, context.getApiCode()));
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.GOME_DATA_COLLECTION;
    }

    @Data
    public static class GomeRuleNecessaryData extends RuleNecessaryData {

        /**
         * 客户上传表信息
         */
        private Map<String, SyncUserValidityPeriodBO> syncUserValidityPeriodMap;

    }
}
