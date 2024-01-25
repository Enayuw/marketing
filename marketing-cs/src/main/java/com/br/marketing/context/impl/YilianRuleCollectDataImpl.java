package com.br.marketing.context.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Service;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.TransferDataValidityPeriodService;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 亿联转化数据过滤规则所需数据收集
 *
 * @author senyang.zheng
 * @date 2024/01/10
 */
@Service
public class YilianRuleCollectDataImpl extends CommonMethodHandlerService {

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>)transmitFacts;
            Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Date yesterday = DateUtil.yesterday();
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                transferDataValidityPeriodService.getValidityPeriodsByCustNum(set, context.getApiCode(), yesterday);
            YilianRuleCollectDataImpl.YilianRuleNecessaryData yilianRuleNecessaryData = new YilianRuleCollectDataImpl.YilianRuleNecessaryData();
            yilianRuleNecessaryData.setCustomerMap(validityPeriodsByCustNum);
            context.setRuleNecessaryData(yilianRuleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.YILIAN_TRANSFER_FILTER_COLLECTION;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public class YilianRuleNecessaryData extends RuleNecessaryData {
        /**
         * 亿联转化所需数据
         */
        private Map<String, SyncUserValidityPeriodsBO> customerMap;
    }
}
