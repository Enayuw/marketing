package com.br.marketing.rule.zhongyou;

import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;

/**
 * @Description ZhongYouCustomerTransferImpl
 * @Author hong.chen
 * @CreateTime 2023/08/07
 */
public class ZhongYouCustomerTransferImpl implements AssembleData<ConversionData> {
    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return false;
    }

    @Override
    public String label() {
        return "ZhongYou_TransferData_CustomerTransfer";
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
