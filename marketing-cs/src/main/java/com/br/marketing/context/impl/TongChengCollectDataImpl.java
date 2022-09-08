package com.br.marketing.context.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description :同程金融转化上下文处理
 * @Author : lizhen
 * @Date : Create in 2022/8/29 13:51
 */
@Service
@Slf4j
public class TongChengCollectDataImpl extends CommonMethodHandlerService {

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            TongChengCollectDataImpl.TongChengRuleNecessaryData tongChengRuleNecessaryData = new TongChengCollectDataImpl.TongChengRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, MarketingSyncUser> collect = customerMarketingSyncUser(set, context.getApiCode());
            tongChengRuleNecessaryData.setCustomerMap(collect);
            context.setRuleNecessaryData(tongChengRuleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.TONG_CHENG_DATA_COLLECTION;
    }


    @Data
    public class TongChengRuleNecessaryData extends RuleNecessaryData {
        /**
         * 同程金融转化所需信息
         */
        private Map<String, MarketingSyncUser> customerMap;
    }


}
