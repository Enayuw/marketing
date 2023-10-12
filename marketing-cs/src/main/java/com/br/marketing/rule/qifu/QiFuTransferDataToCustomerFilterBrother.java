package com.br.marketing.rule.qifu;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.qifu.util.QiFuTransferDataUtil;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 描述：： 360 转化数据推客服过滤
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName QiFuTransferDataToCustomerFilter
 * @author: it-yml
 * @create: 2023-09-27 16:48
 * @Version 1.0
 * --------------------------------------
 **/
@Service
@Slf4j
public class QiFuTransferDataToCustomerFilterBrother implements AssembleData<ConversionData> {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return QiFuTransferDataUtil.getConversionData((MarketingTransferSyncUser) transmitFact, context);
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {

            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String custNum = transfer.getCustNum();
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = QiFuTransferDataUtil.getSyncUserValidityPeriodsBO(context, custNum);
            if(syncUserValidityPeriodsBO!=null){
                String applyDt = transfer.getApplyDt();
                return QiFuTransferDataUtil.isNeedAssmble(transfer, syncUserValidityPeriodsBO)
                        &&
                        QiFuTransferDataUtil.isRuleAssmble(applyDt, custNum, syncUserValidityPeriodsBO);
            }

        }
        return false;


    }


    @Override
    public String label() {
        return "QiFu_TransferData_To_CustomerFilter_Brother";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE_QIFU.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.QIFU360_DATA_COLLECTION.getCode();
    }
}
