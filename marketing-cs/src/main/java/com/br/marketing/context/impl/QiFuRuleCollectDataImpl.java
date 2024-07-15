package com.br.marketing.context.impl;

import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.TransferDataValidityPeriodService;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 描述：： 360有效期规则数据
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName QiFuRuleCollectDataImpl
 * @author: it-yml
 * @create: 2023-09-27 17:44
 * @Version 1.0
 * --------------------------------------
 **/
@Service
public class QiFuRuleCollectDataImpl  extends CommonMethodHandlerService{
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;
    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNumAndTaskId(set, context.getApiCode(), new Date());
            QiFuRuleCollectDataImpl.QiFuRuleNecessaryData qiFuRuleNecessaryData = new QiFuRuleCollectDataImpl.QiFuRuleNecessaryData();
            qiFuRuleNecessaryData.setCustomerMap(validityPeriodsByCustNum);
            context.setRuleNecessaryData(qiFuRuleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.QIFU360_DATA_COLLECTION;
    }


    @Data
    public class QiFuRuleNecessaryData extends RuleNecessaryData {
        /**
         * 360转化所需信息
         */
        private Map<String, SyncUserValidityPeriodsBO> customerMap;
    }

}
