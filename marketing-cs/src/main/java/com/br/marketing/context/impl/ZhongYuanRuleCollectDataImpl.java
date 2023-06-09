package com.br.marketing.context.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhen.Li
 * @version 1.0
 * @date 2023/06/08 19:45
 * 中原规则所需要的数据
 */
@Service
public class ZhongYuanRuleCollectDataImpl extends CommonMethodHandlerService {


    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty()) {
            Object o = transmitFacts.get(0);
            ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData ruleNecessaryData = new ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData();
            if (o instanceof MarketingTransferSyncUser) {
                @SuppressWarnings("unchecked")
                List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
                Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                Map<String, MarketingSyncUser> collect = customerMarketingSyncUser(set, context.getApiCode());
                ruleNecessaryData.setCustomerMap(collect);
            } else if (o instanceof CallRecordBO) {
                @SuppressWarnings("unchecked")
                Set<String> set = ((List<CallRecordBO>) transmitFacts).stream()
                        .map(CallRecordBO::getCaseNum).collect(Collectors.toSet());
                ruleNecessaryData.setCallRecordCustomerMap(customerMarketingSyncUser(set, context.getApiCode()));
            }
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.ZHONGYUAN_DATA_COLLECTION;
    }


    @Data
    public class ZhongYuanRuleNecessaryData extends RuleNecessaryData {
        /**
         * 客户上传信息
         */
        private Map<String, MarketingSyncUser> customerMap;

        /**
         * 拨打明细原始数据
         */
        private Map<String, MarketingSyncUser> callRecordCustomerMap;


    }


}
