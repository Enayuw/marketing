package com.br.marketing.context.impl;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;
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
 * @date 2023/3/24 17:17
 */

@Service
public class ElephantCollectDataImpl extends CommonMethodHandlerService{



    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            ElephantRuleNecessaryData ruleNecessaryData = new ElephantRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            ruleNecessaryData.setSyncUserValidityPeriodMap(newCustomerSyncUserValidityPeriod(transferList, context.getApiCode()));
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.ELEPHANT_DATA_COLLECTION;
    }





    @Data
    public class ElephantRuleNecessaryData extends RuleNecessaryData {

        /**
         * 客户上传表信息
         */
        private  Map<String, SyncUserValidityPeriodsBO>  syncUserValidityPeriodMap;

    }
}
