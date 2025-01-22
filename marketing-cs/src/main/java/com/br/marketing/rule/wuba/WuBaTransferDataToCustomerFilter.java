package com.br.marketing.rule.wuba;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.common.util.StringUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.WuBaRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 58新客转化数据推送客服
 *
 * @Author lixiang
 * @Date 2024-10-19
 */
@Service
@Slf4j
public class WuBaTransferDataToCustomerFilter implements AssembleData<ConversionData> {

    private static final String TITLE = "【58新客转化数据推送客服】";

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser marketingTransferSyncUser = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(marketingTransferSyncUser.getId().toString());
        conversionData.setCid(marketingTransferSyncUser.getCid());
        conversionData.setCaseNum(marketingTransferSyncUser.getCustNum());
        conversionData.setPartnerProcessDate(DateUtils.format(marketingTransferSyncUser.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        WuBaRuleCollectDataImpl.WuBaRuleNecessaryData ruleNecessaryData =
                (WuBaRuleCollectDataImpl.WuBaRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, SyncUserValidityPeriodsBO> userValidityPeriodsBOMap = ruleNecessaryData.getSyncUserValidityPeriodMap();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = userValidityPeriodsBOMap.get(marketingTransferSyncUser.getCustNum());
        List<MarketingSyncUser> syncUsers = syncUserValidityPeriodsBO.getSyncUsers();
        conversionData.setPhone(BrCipherMaker.getInstance().decode(syncUsers.get(0).getCell()));
        // conversionData.setGroupType(syncUsers.get(0).getUserType());

        PeriodOfValidityBO periodOfValidityBO = syncUserValidityPeriodsBO.getBuilders().get(0).addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(marketingTransferSyncUser, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (!(transmitFact instanceof MarketingTransferSyncUser)) {
            return false;
        }
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;

        WuBaRuleCollectDataImpl.WuBaRuleNecessaryData ruleNecessaryData =
                (WuBaRuleCollectDataImpl.WuBaRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, SyncUserValidityPeriodsBO> userValidityPeriodsBOMap = ruleNecessaryData.getSyncUserValidityPeriodMap();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = userValidityPeriodsBOMap.get(transfer.getCustNum());
        if(syncUserValidityPeriodsBO == null){
            return false;
        }

        String applyResult = transfer.getApplyResult();
        if (StringUtils.isEmpty(applyResult) || !"1".equals(applyResult)) {
            log.warn(TITLE+"-isNeedAssemble, applyResult: {}", applyResult);
            return false;
        }
        return true;
    }

    @Override
    public String label() {
        return "WuBa_TransferData_To_Customer_Filter";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.WUBA_TRANSFER_FILTER_COLLECTION.getCode();
    }

}
