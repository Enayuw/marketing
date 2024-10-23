package com.br.marketing.rule.weiju;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.WeiJuRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 【紧急】D20241022微聚自动化过滤-3710157
 * 需求：https://c.100credit.cn/pages/viewpage.action?pageId=184391714
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-10-23
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WeiJuTransferDataAutoFiltrationImpl implements AssembleData<ConversionData> {

    private final static String CASE_EFFECTIVE_0="0";
    private final static String INVERSION_STATUS_2="2";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        String custNum = transfer.getCustNum();
        conversionData.setCaseNum(custNum);
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transfer.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transfer.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        conversionData.setInversionStatus(INVERSION_STATUS_2);
        WeiJuRuleCollectDataImpl.WeiJuRuleNecessaryData data =
                (WeiJuRuleCollectDataImpl.WeiJuRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, SyncUserValidityPeriodsBO> syncUserValidityPeriodMap = data.getSyncUserValidityPeriodMap();
        SyncUserValidityPeriodsBO bo = syncUserValidityPeriodMap.get(custNum);
        MarketingSyncUser marketingSyncUser = bo.getSyncUsers().get(0);
        PeriodOfValidityBO.Builder builder = bo.getBuilders().get(0);
        conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        PeriodOfValidityBO periodOfValidityBO = builder.addDateString().addOfDayTimeStrString().builder();
        // 有效期设置
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        // 去重参数设置
        conversionData.setInitId(transfer.getId());
        conversionData.setSoleField(SoleFieldEnum.CUST_NUM_SOLE.getValue());
        conversionData.setSoleType(1);
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String caseEffective = transfer.getCaseEffective();
            if(StringUtils.isNotBlank(caseEffective) && CASE_EFFECTIVE_0.equalsIgnoreCase(caseEffective)){
                WeiJuRuleCollectDataImpl.WeiJuRuleNecessaryData data =
                        (WeiJuRuleCollectDataImpl.WeiJuRuleNecessaryData) context.getRuleNecessaryData();
                if (data.getSyncUserValidityPeriodMap().get(transfer.getCustNum()) == null) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "WeiJu_TransferData_Customer_Auto_Filtration";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.WEIJU_TRANSFER_FILTER_COLLECTION.getCode();
    }
}
