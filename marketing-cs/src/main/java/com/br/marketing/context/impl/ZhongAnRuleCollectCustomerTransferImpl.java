package com.br.marketing.context.impl;


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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                    transferDataValidityPeriodService.getValidityPeriodsByCellAndUserType(set,"1", context.getApiCode(), new Date());
            ZhongAnRuleCollectCustomerTransferImpl.ZhongAnRuleNecessaryData zhongBangRuleNecessaryData = new ZhongAnRuleCollectCustomerTransferImpl.ZhongAnRuleNecessaryData();
            zhongBangRuleNecessaryData.setCustomerMap(validityPeriodsByCustNum);
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
         * 360转化所需信息
         */
        private Map<String, SyncUserValidityPeriodsBO> customerMap;

    }
}
