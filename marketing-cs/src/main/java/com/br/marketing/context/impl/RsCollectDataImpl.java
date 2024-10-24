package com.br.marketing.context.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class RsCollectDataImpl extends CommonMethodHandlerService{

    @Resource
    private TableCreateServiceImpl tableCreateService;


    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            RsRuleNecessaryData ruleNecessaryData = new RsRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            ruleNecessaryData.setCustomerMap(customerMarketingSyncUser(set,context.getApiCode()));
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.RS_DATA_COLLECTION;
    }





    @Data
    public class RsRuleNecessaryData extends RuleNecessaryData {

        /**
         * 客户上传表信息
         */
        private Map<String, MarketingSyncUser> customerMap;

    }
}
