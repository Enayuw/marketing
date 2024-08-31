package com.br.marketing.rule.yishi;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YishiRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * D20240522医时自动化过滤-3710136
 * https://c.100credit.cn/pages/viewpage.action?pageId=160811114
 *
 * @author guangxiu.li
 * @dateTime 2024/05/24 9:10
 */
@Service
@Slf4j
public class YiShiTransferAutomaticFiltrationImpl implements AssembleData<ConversionData> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        YishiRuleCollectDataImpl.YishiRuleNecessaryData data =
                (YishiRuleCollectDataImpl.YishiRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus(data.getInversionStatus());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transfer.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transfer.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws IllegalAccessException {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if(StringUtils.isEmpty(reserveField1)){
                return false;
            }
            YishiRuleCollectDataImpl.YishiRuleNecessaryData data =
                    (YishiRuleCollectDataImpl.YishiRuleNecessaryData) context.getRuleNecessaryData();

            JSONObject jsonObject = JSONObject.parseObject(reserveField1);
            String isBlack = jsonObject.getString("isBlack");
            if(StringUtils.isNotEmpty(isBlack) && "1".equals(isBlack)){
                data.setInversionStatus("2");
                return true;
            }
            String extend01 = jsonObject.getString("extend01");
            if(StringUtils.isNotEmpty(extend01) && "1".equals(extend01)){
                data.setInversionStatus("0");
                return true;
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "YiShi_TransferData_Customer_Auto_Filtration";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YISHI_TRANSFER_FILTER_COLLECTION.getCode();
    }
}
