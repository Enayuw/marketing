package com.br.marketing.rule.qifu;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.QiFuRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.qifu.util.QiFuTransferDataUtil;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * D20240622促动支自动化过滤-3710139（营销→外呼）
 *
 * @ClassName QiFuTransferDataToCustomerFilterCuDongZhi
 * @author: lixiang
 * @create: 2023-09-27 16:48
 **/
@Service
@Slf4j
public class QiFuTransferDataToCustomerFilterCuDongZhi implements AssembleData<ConversionData> {

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));

        QiFuRuleCollectDataImpl.QiFuRuleNecessaryData ruleNecessaryData =
                (QiFuRuleCollectDataImpl.QiFuRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, SyncUserValidityPeriodsBO> syncUserPeriodMap = ruleNecessaryData.getCustomerMap();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = syncUserPeriodMap.get(transfer.getCustNum());
        if (syncUserValidityPeriodsBO == null) {
            return null;
        }
        // 去重参数设置
        conversionData.setInitId(transfer.getId());
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
        if (!(transmitFact instanceof MarketingTransferSyncUser)) {
            return false;
        }
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        String custNum = transfer.getCustNum();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = QiFuTransferDataUtil.getSyncUserValidityPeriodsBO(context, custNum);
        if (syncUserValidityPeriodsBO == null) {
            return false;
        }

        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.isEmpty(reserveField1)) {
            return false;
        }
        JSONObject jo = JSONObject.parseObject(reserveField1);
        if (jo == null) {
            return false;
        }
        String applyLoan = jo.getString("applyLoan");
        if (StringUtils.isEmpty(applyLoan) || !"1".equals(applyLoan)) {
            return false;
        }

        return true;
    }

    @Override
    public String label() {
        return "QiFu_TransferData_To_Customer_Filter_CuDongZhi";
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
