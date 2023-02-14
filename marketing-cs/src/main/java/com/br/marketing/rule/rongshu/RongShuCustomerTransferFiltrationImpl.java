package com.br.marketing.rule.rongshu;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * 榕树转化数据自动过滤推客服
 * @author GuangChao.Zhang
 * @version 1.0
 * @Date 2023/2/13 17:52
 */
@Service
@Slf4j
public class RongShuCustomerTransferFiltrationImpl implements AssembleData<ConversionData> {


    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        RuleNecessaryData ruleNecessaryData = context.getRuleNecessaryData();
        conversionData.setPhone(BrCipherMaker.getInstance().decode(transfer.getCustNum()));

        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return false;
    }

    @Override
    public String label() {
         return "RongShu_Customer_TransferData_Filtration";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
