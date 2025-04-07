package com.br.marketing.context.impl;

import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.TransferDataValidityPeriodService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description 榕树新客自运营ab意向自动化转Daas-4004739/4004713 数据收集
 * @author hedongshuo
 * @date 2025/3/27 16:56
 **/
@Service
public class RsxkCollectDataImpl extends CommonMethodHandlerService {

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        RsxkRuleNecessaryData rsxkRuleNecessaryData = new RsxkRuleNecessaryData();
        if(!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof CallRecordBO){
            Set<String> custNumSet = ((List<CallRecordBO>) transmitFacts).stream().map(CallRecordBO::getCaseNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodBO> syncUserPeriodMap = transferDataValidityPeriodService
                    .getValidityPeriodCustNumBatchFirstVersion(custNumSet, context.getApiCode(), new Date());
            rsxkRuleNecessaryData.setSyncUserPeriodMap(syncUserPeriodMap);
        }
        context.setRuleNecessaryData(rsxkRuleNecessaryData);
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.RSXK_DATA_COLLECTION;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public class RsxkRuleNecessaryData extends RuleNecessaryData {

        /**
         * 有效期内最新上传数据
         */
        private Map<String, SyncUserValidityPeriodBO> syncUserPeriodMap;

    }




}
