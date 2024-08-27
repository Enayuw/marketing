package com.br.marketing.rule.zhongan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ZhongAnRuleCollectCustomerTransferImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ZhongAnTransferDataByUserType8Filter
 * @Description TODO
 * @Author kongbx
 * @Date 2024/8/27 16:30
 */
@Service
@Slf4j
public class ZhongAnTransferDataByUserType8Filter implements AssembleData<ConversionData> {
    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser marketingTransferSyncUser = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(marketingTransferSyncUser.getId().toString());
        conversionData.setCid(marketingTransferSyncUser.getCid());
        conversionData.setCaseNum(marketingTransferSyncUser.getCustNum());
        conversionData.setPartnerProcessDate(DateUtils.format(marketingTransferSyncUser.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        ZhongAnRuleCollectCustomerTransferImpl.ZhongAnRuleNecessaryData ruleNecessaryData =
                (ZhongAnRuleCollectCustomerTransferImpl.ZhongAnRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, SyncUserValidityPeriodsBO> syncUserPeriodMap = ruleNecessaryData.getCustomerMap();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = syncUserPeriodMap.get(marketingTransferSyncUser.getCustNum());
        if (syncUserValidityPeriodsBO == null) {
            return null;
        }
        List<MarketingSyncUser> syncUsers = syncUserValidityPeriodsBO.getSyncUsers();
        conversionData.setPhone(BrCipherMaker.getInstance().decode(syncUsers.get(0).getCell()));
        conversionData.setGroupType(syncUsers.get(0).getUserType());
        // 去重参数设置
        conversionData.setInitId(marketingTransferSyncUser.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_STATUS_SOLE.getValue());
        conversionData.setSoleType(-1);
        PeriodOfValidityBO periodOfValidityBO = syncUserValidityPeriodsBO.getBuilders().get(0).addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            ZhongAnRuleCollectCustomerTransferImpl.ZhongAnRuleNecessaryData ruleNecessaryData =
                    (ZhongAnRuleCollectCustomerTransferImpl.ZhongAnRuleNecessaryData) context.getRuleNecessaryData();
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
            if (syncUserValidityPeriodsBO == null) {
                log.warn("众安转化数据推客服过滤数据不在有效期：{}", transfer.getCustNum());
                return false;
            }
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.isBlank(reserveField1)) {
                log.warn("众安转化数据推客服过滤数据reserveField1为空：{}", transfer.getCustNum());
                return false;
            }
            JSONObject jsonObjectReserveField1 = JSON.parseObject(reserveField1);
            String eventType = jsonObjectReserveField1.getString("eventType");
            String userType = jsonObjectReserveField1.getString("userType");
            if(!"8".equals(userType)){
                log.warn("众安转化数据推客服过滤数据userType不符合规则5：{}", userType);
                return false;
            }
            if ("LOAN_APPLY".equals(eventType) || "WITHDRAW_SUCCESS".equals(eventType)) {
                return true;
            }
            log.warn("众安转化数据推客服过滤数据eventType不符合推送要求：{}", transfer.getCustNum());
        }
        return false;
    }

    @Override
    public String label() {
        return "ZhongAn_TransferData_To_CustomerFilter";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE_STATUS.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.ZHONGAN_TRANSFER_FILTER_COLLECTION.getCode();
    }
}
