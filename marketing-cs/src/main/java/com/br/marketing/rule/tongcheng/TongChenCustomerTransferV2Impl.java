package com.br.marketing.rule.tongcheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.TongChengCollectDataV2Impl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * D20230727同程自动化过滤-3710062（营销→外呼）
 * http://c.100credit.cn/pages/viewpage.action?pageId=119723248
 *
 * @author Guo Zeqiang
 * @dateTime 2023/8/1 14:23
 */
@Service
@Slf4j
public class TongChenCustomerTransferV2Impl implements AssembleData<ConversionData> {

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        ConversionData conversionData = new ConversionData();
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setInversionStatus("0");
        conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            TongChengCollectDataV2Impl.TongChengRuleNecessaryData tongChengContext
                    = (TongChengCollectDataV2Impl.TongChengRuleNecessaryData)
                    context.getRuleNecessaryData();
            SyncUserValidityPeriodBO bo = tongChengContext.getCustomerMap().get(
                    transfer.getCustNum() + transfer.getUserType());
            // 判断有效期
            if (bo != null) {
                String applyResult = transfer.getApplyResult();
                String applyLoan;
                if (StringUtils.isNotBlank(transfer.getReserveField1())) {
                    // 获取扩展字段属性
                    JSONObject jsonObject = JSON.parseObject(transfer.getReserveField1());
                    applyLoan = jsonObject.getString("applyLoan");
                } else {
                    applyLoan = "";
                }
                switch (transfer.getUserType()) {
                    // 场景2中判断满足推送条件的数据
                    case "2":
                        return "0".equals(applyResult)
                                || applyResult == null
                                || "".equals(applyResult)
                                || ("1".equals(applyResult) && "1".equals(applyLoan));
                    // 场景3、5中判断满足推送条件的数据
                    case "3":
                    case "5":
                        return "0".equals(applyLoan) || "1".equals(applyLoan);
                    default:
                }
            }
        }
        return false;
    }


    @Override
    public String label() {
        return "TongCheng_TransferData_CustomerTransfer_v2";
    }


    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }


    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.TONG_CHENG_DATA_COLLECTION_V2.getCode();
    }

}
