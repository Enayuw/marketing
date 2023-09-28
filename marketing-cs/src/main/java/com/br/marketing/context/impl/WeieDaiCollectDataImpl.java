package com.br.marketing.context.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.dto.customer.CallRecordBO;
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
 * 微e贷数据收集实现
 *
 * @author senyang.zheng
 * @date 2023/09/20
 */
@Service
public class WeieDaiCollectDataImpl extends CommonMethodHandlerService {

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (CollectionUtil.isNotEmpty(transmitFacts)) {
            Object o = transmitFacts.get(0);
            WeieDaiNecessaryData ruleNecessaryData = new WeieDaiNecessaryData();
            if (o instanceof CallRecordBO) {
                @SuppressWarnings("unchecked")
                Set<String> custNumSet = ((List<CallRecordBO>) transmitFacts).stream().map(CallRecordBO::getCaseNum).collect(Collectors.toSet());
                Map<String, SyncUserValidityPeriodBO> syncUserPeriodMap = transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(custNumSet, context.getApiCode(), new Date());
                ruleNecessaryData.setSyncUserPeriodMap(syncUserPeriodMap);
            }
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.WEIEDAI_DATA_COLLECTION;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public class WeieDaiNecessaryData extends RuleNecessaryData {

        /**
         * 有效期内最新上传数据
         */
        private Map<String, SyncUserValidityPeriodBO> syncUserPeriodMap;

    }
}
