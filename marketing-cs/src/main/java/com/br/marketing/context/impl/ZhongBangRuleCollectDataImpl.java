package com.br.marketing.context.impl;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhen.Li
 * @version 1.0
 * @date 2023/08/01 16:45
 * 众邦规则所需要的数据
 */
@Service
public class ZhongBangRuleCollectDataImpl extends CommonMethodHandlerService {

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty()) {
            Object o = transmitFacts.get(0);
            ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData ruleNecessaryData =
                    new ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData();
            if (o instanceof MarketingTransferSyncUser) {
                @SuppressWarnings("unchecked")
                List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
                Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                Map<String, SyncUserValidityPeriodsBO> syncUser =
                        transferDataValidityPeriodService.getValidityPeriodsByCustNum(set, context.getApiCode(), new Date());
                ruleNecessaryData.setCustomerMap(syncUser);
            } else if (o instanceof CallRecordBO) {

                @SuppressWarnings("unchecked")
                String apiCode = ((CallRecordBO) o).getApiCode();
                String tcid = tableCreateService.getTcId(apiCode);
                Set<String> set = ((List<CallRecordBO>) transmitFacts).stream()
                        .map(CallRecordBO::getCaseNum).collect(Collectors.toSet());
                Map<String, SyncUserValidityPeriodsBO> callRecordSyncUser =
                        transferDataValidityPeriodService.getValidityPeriodsByCustNum(set, context.getApiCode(), new Date());
                ruleNecessaryData.setCallRecordCustomerMap(callRecordSyncUser);
                List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                        marketingTransferSyncUserMapper.getTransferByCustNumOrderDatatikv_(tcid, new ArrayList<>(set));

                ruleNecessaryData.setTransferMap(marketingTransferSyncUserList.stream()
                        .collect(Collectors.toMap(MarketingTransferSyncUser::getCustNum, Function.identity())));

            }
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.ZHONGBANG_DATA_COLLECTION;
    }


    @Data
    public class ZhongBangRuleNecessaryData extends RuleNecessaryData {
        /**
         * 拨打明细原始数据
         */
        private Map<String, SyncUserValidityPeriodsBO> callRecordCustomerMap;

        /**
         * 存在有效期的上传数据
         */
        private Map<String, SyncUserValidityPeriodsBO> customerMap;

        /**
         * 最新的转化数据
         */
        private Map<String, MarketingTransferSyncUser> transferMap;

    }


}
