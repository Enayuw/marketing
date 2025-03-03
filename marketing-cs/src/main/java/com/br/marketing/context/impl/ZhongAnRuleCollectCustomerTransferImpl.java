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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 众邦规则所需要的数据
 * @author guangchao.zhang
 * @version 1.0
 * @date 2023/12/06 16:45
 */
@Service
public class ZhongAnRuleCollectCustomerTransferImpl extends CommonMethodHandlerService{

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;
    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Set<String> userTypeSet = new HashSet<>();
            userTypeSet.add("1");
            userTypeSet.add("2");
            userTypeSet.add("4");
            userTypeSet.add("7");
            userTypeSet.add("8");
            Map<String, Map<String, SyncUserValidityPeriodsBO>> validityPeriodsByCustNum =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNumAndUserTypeSet(set, userTypeSet, context.getApiCode(), new Date());
            ZhongAnRuleCollectCustomerTransferImpl.ZhongAnRuleNecessaryData zhongBangRuleNecessaryData = new ZhongAnRuleCollectCustomerTransferImpl.ZhongAnRuleNecessaryData();
            zhongBangRuleNecessaryData.setCustomerUserTypeMap(validityPeriodsByCustNum);
            context.setRuleNecessaryData(zhongBangRuleNecessaryData);
        }
    }
    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.ZHONGAN_TRANSFER_FILTER_COLLECTION;
    }


    @Data
    public class ZhongAnRuleNecessaryData extends RuleNecessaryData {
        /**
         * @deprecated 转化所需要的数据
         */
        @Deprecated
        private Map<String, SyncUserValidityPeriodsBO> customerMap;
        /**
         * 转化所需要的有效期数据
         */
        private Map<String, Map<String, SyncUserValidityPeriodsBO>> customerUserTypeMap;

    }
}
