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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description 数禾促首登规则数据收集实现类
 * @Author hong.chen
 * @CreateTime 2024/06/25
 */
@Service
public class ShuHeCuShouDengRuleCollectDataImpl extends CommonMethodHandlerService {
    @Resource
    private TransferDataValidityPeriodService validityPeriodService;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            ShuHeCuShouDengRuleNecessaryData necessaryData = new ShuHeCuShouDengRuleNecessaryData();
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFacts.get(0);

            Set<String> custNumSet = new HashSet<>();
            String custNum = transfer.getCustNum();
            custNumSet.add(custNum);
            Map<String, SyncUserValidityPeriodsBO> boMap = validityPeriodService.getValidityPeriodsByCustNum(custNumSet, context.getApiCode(),
                    new Date());

            necessaryData.setPeriodBOMap(boMap);
            context.setRuleNecessaryData(necessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.SHUHE_CUSHOUDENG_RULE_DATA_COLLECTION;
    }

    @Data
    public class ShuHeCuShouDengRuleNecessaryData extends RuleNecessaryData {
        /**
         * 有效期内最新一条上传数据
         */
        private Map<String, SyncUserValidityPeriodsBO> periodBOMap;
    }
}
