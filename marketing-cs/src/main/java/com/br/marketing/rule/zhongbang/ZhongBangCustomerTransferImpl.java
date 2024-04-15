package com.br.marketing.rule.zhongbang;

import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ZhongBangRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;


/**
 * @Description 众邦推客服转化
 * @Author zhen.Li
 * @CreateTime 2023/08/23
 */
@Service
@Slf4j
public class ZhongBangCustomerTransferImpl implements AssembleData<ConversionData> {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData ruleNecessaryData =
                (ZhongBangRuleCollectDataImpl.ZhongBangRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, SyncUserValidityPeriodsBO> syncUserPeriodMap = ruleNecessaryData.getCustomerMap();
        SyncUserValidityPeriodsBO bo = syncUserPeriodMap.get(transfer.getCustNum());
        if (bo == null) {
            return null;
        }
        MarketingSyncUser marketingSyncUser = bo.getSyncUsers().get(0);
        conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        // 去重参数设置
        conversionData.setInitId(transfer.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        PeriodOfValidityBO periodOfValidityBO = bo.getBuilders().get(0).addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            if (marketingCommonConfig.getZhongBangToAIFirstSwitch()) {
                return "1".equals(transfer.getIfLogin());
            } else {
                //非首次
                LocalDate loginTime = DateHelper.strToLocalDate(transfer.getLoginTime());
                return "1".equals(transfer.getIfLogin()) && loginTime.isEqual(LocalDate.now().minusDays(1));
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "ZhongBang_TransferData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.ZHONGBANG_DATA_COLLECTION.getCode();
    }


}
