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

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/4/3 10:45
 * 国美规则所需要的数据
 */
@Service
public class GomeRuleCollectDataImpl extends CommonMethodHandlerService{
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            GomeRuleCollectDataImpl.GomeRuleNecessaryData ruleNecessaryData = new GomeRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = transferDataValidityPeriodService.getValidityPeriodsByCustNum(set, context.getApiCode(), new Date());
            ruleNecessaryData.setSyncUserValidityPeriodMap(validityPeriodsByCustNum);
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
        private Map<String, SyncUserValidityPeriodsBO> syncUserValidityPeriodMap;

    }
}
