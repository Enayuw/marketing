package com.br.marketing.rule.qifu;

import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.QiFuRuleCollectDataImpl;
import com.br.marketing.context.impl.ZhongBangRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

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
public class QiFuTransferDataToCustomerFilter implements AssembleData<ConversionData> {
    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        QiFuRuleCollectDataImpl.QiFuRuleNecessaryData ruleNecessaryData =
                (QiFuRuleCollectDataImpl.QiFuRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, SyncUserValidityPeriodBO> syncUserPeriodMap = ruleNecessaryData.getCustomerMap();
        SyncUserValidityPeriodBO bo = syncUserPeriodMap.get(transfer.getCustNum());
        if (bo == null) {
            return null;
        }
        conversionData.setPhone(BrCipherMaker.getInstance().decode(bo.getSyncUser().getCell()));
        // 去重参数设置
        conversionData.setInitId(transfer.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        PeriodOfValidityBO periodOfValidityBO = bo.getBuilder().addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            // 1. 有效期判断
            // 2. 公共方法调用
            String loginTime = transfer.getLoginTime();
        }

        return false;
    }

    @Override
    public String label() {
        return "QiFu_TransferData_To_CustomerFilter";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.QIFU360_DATA_COLLECTION.getCode();
    }
}
