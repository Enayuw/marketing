package com.br.marketing.context.impl;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.TransferDataValidityPeriodService;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class SaMoYeCollectDataImpl extends CommonMethodHandlerService{

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            SaMoYeRuleNecessaryData ruleNecessaryData = new SaMoYeRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Set<String> custNumSet = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Set<String> userTypeSet = transferList.stream().map(MarketingTransferSyncUser::getUserType).collect(Collectors.toSet());
            // 场景有效期
            Map<String, Map<String, SyncUserValidityPeriodsBO>> validityPeriodsByCustNum =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNumAndUserTypeSet(custNumSet, userTypeSet, context.getApiCode(), new Date());
            ruleNecessaryData.setCustomerUserTypeMap(validityPeriodsByCustNum);
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }
    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.SAMOYE_TRANSFER_FILTER_COLLECTION;
    }
    @Data
    public class SaMoYeRuleNecessaryData extends RuleNecessaryData {
        /**
         * 转化所需要的有效期数据
         */
        private Map<String, Map<String, SyncUserValidityPeriodsBO>> customerUserTypeMap;
        /**
         * 是否已转化
         */
        private String inversionStatus;
    }
}
