package com.br.marketing.rule.yilian;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YilianRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;

import lombok.extern.slf4j.Slf4j;

/**
 * 亿联转化数据自动过滤推客服
 *
 * @author senyang.zheng
 * @date 2024/01/09
 */
@Service
@Slf4j
public class YilianTransferDataToCustomerFilter implements AssembleData<ConversionData> {

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser marketingTransferSyncUser = (MarketingTransferSyncUser)transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(marketingTransferSyncUser.getId().toString());
        conversionData.setCid(marketingTransferSyncUser.getCid());
        conversionData.setCaseNum(marketingTransferSyncUser.getCustNum());
        conversionData.setPartnerProcessDate(DateUtils.format(marketingTransferSyncUser.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        YilianRuleCollectDataImpl.YilianRuleNecessaryData ruleNecessaryData =
            (YilianRuleCollectDataImpl.YilianRuleNecessaryData)context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, SyncUserValidityPeriodsBO> syncUserPeriodMap = ruleNecessaryData.getCustomerMap();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = syncUserPeriodMap.get(marketingTransferSyncUser.getCustNum());
        if (syncUserValidityPeriodsBO == null) {
            return null;
        }
        // 去重参数设置
        conversionData.setSoleField(SoleFieldEnum.CUST_NUM_SOLE.getValue());
        conversionData.setSoleType(-1);
        PeriodOfValidityBO periodOfValidityBO = syncUserValidityPeriodsBO.getBuilders().get(0).addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
            YilianRuleCollectDataImpl.YilianRuleNecessaryData ruleNecessaryData =
                (YilianRuleCollectDataImpl.YilianRuleNecessaryData)context.getRuleNecessaryData();
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
            if (syncUserValidityPeriodsBO == null) {
                log.warn("亿联转化数据推客服过滤数据不在有效期：{}", transfer.getCustNum());
                return false;
            }
            String ifApply = transfer.getIfApply();
            if ("1".equals(ifApply)) {
                return true;
            }
            log.warn("亿联转化数据推客服过滤数据ifApply不符合推送要求：{}", transfer.getCustNum());
        }
        return false;
    }

    @Override
    public String label() {
        return "Yilian_TransferData_To_CustomerFilter";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YILIAN_TRANSFER_FILTER_COLLECTION.getCode();
    }
}
