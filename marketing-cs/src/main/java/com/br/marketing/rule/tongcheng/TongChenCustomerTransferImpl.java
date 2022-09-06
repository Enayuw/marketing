package com.br.marketing.rule.tongcheng;

import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.TongChengCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 同程金融推送转化至客服转化 业务
 *
 * @author lizhen
 * @dateTime 2022/8/29 15:35
 */
@Service
@Slf4j
public class TongChenCustomerTransferImpl implements AssembleData<ConversionData> {

    @Resource
    private DataLoadingHandlerService handlerService;

    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setInversionStatus("0");
        conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        Boolean bool1 = Boolean.FALSE;
        Boolean bool2 = Boolean.FALSE;
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        TongChengCollectDataImpl.TongChengRuleNecessaryData tongChengContext = (TongChengCollectDataImpl.TongChengRuleNecessaryData)
                context.getRuleNecessaryData();
        MarketingSyncUser syncUser = tongChengContext.getCustomerMap().get(transfer.getCustNum());
        if (syncUser == null) {
            return false;
        }
        Integer day = handlerService.getTongChengPeriodOfValidityDay();

        if (iMarketingSyncUserService.isPeriodOfValidity(transfer.getCreateTime(), day, syncUser.getCreateTime())) {
            bool1 = ("1").equals(transfer.getApplyResult()) && ("1").equals(transfer.getIfLent());
            bool2 = "0".equals(transfer.getApplyResult());
        }
        return bool1 || bool2;
    }


    @Override
    public String label() {
        return "TongCheng_TransferData_CustomerTransfer";
    }


    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }


    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.TONG_CHENG_DATA_COLLECTION.getCode();
    }

}
