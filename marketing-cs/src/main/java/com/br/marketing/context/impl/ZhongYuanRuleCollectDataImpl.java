package com.br.marketing.context.impl;

import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
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
 * @author zhen.Li
 * @version 1.0
 * @date 2023/06/08 19:45
 * 中原规则所需要的数据
 */
@Service
public class ZhongYuanRuleCollectDataImpl extends CommonMethodHandlerService {
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty()) {
            Object o = transmitFacts.get(0);
            ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData ruleNecessaryData = new ZhongYuanRuleCollectDataImpl.ZhongYuanRuleNecessaryData();
            if (o instanceof CallRecordBO) {
                Set<String> custNumSet = ((List<CallRecordBO>) transmitFacts).stream()
                        .map(CallRecordBO::getCaseNum).collect(Collectors.toSet());

                Map<String, SyncUserValidityPeriodBO> periodBOMap =
                        transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(custNumSet, context.getApiCode(), new Date());
                ruleNecessaryData.setPeriodBOMap(periodBOMap);
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

        /**
         * 有效期内最新一条上传数据
         */
        private Map<String, SyncUserValidityPeriodBO> periodBOMap;


    }
}