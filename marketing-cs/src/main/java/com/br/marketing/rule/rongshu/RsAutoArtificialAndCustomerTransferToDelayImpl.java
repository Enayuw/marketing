package com.br.marketing.rule.rongshu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.RsCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Service
@Slf4j
public class RsAutoArtificialAndCustomerTransferToDelayImpl implements AssembleData<MqFact> {


    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    IPeriodOfValidityService iPeriodOfValidityService;

    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(transfer.getId());
        Set set = new HashSet<>();
        set.add("RongShu_TransferData_ArtificialAndCustomerBatch");
        mqFact.setIncludeRules(set);
        return mqFact;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String userType = transfer.getUserType();
            String auditTime = StringUtils.isBlank(transfer.getAuditTime()) ? "" : transfer.getAuditTime();
            String reserveField1 = transfer.getReserveField1();
            if(StringUtils.isBlank(reserveField1)){
                return false;
            }
            Double unlentAmount = new Double(0);
            try {
                JSONObject jsonObject = JSON.parseObject(reserveField1);
                String unlentAmountStr = jsonObject.getString("unlentAmount");
                if(StringUtils.isNotBlank(unlentAmountStr)){
                    unlentAmount = Double.valueOf(unlentAmountStr);
                }

            }catch (Exception ex){
                log.error(ex.getMessage(),ex);
                return false;
            }

            int rsUnlentAmount = marketingCommonConfig.getRsUnlentAmount() == null ? 1000 : marketingCommonConfig.getRsUnlentAmount();
            if (!"3".equals(userType)) {
                return false;
            }

            String nowDay = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!auditTime.contains(nowDay)) {
                return false;
            }

            if (unlentAmount < rsUnlentAmount) {
                return false;
            }

            RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData =
                    (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
            MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
            if (marketingSyncUser == null) {
                return false;
            }
            String appletDate = marketingSyncUser.getAppletDate();
            if (iPeriodOfValidityService.isExpire(appletDate, marketingCommonConfig.getRsValidityDay(), null)) {
                return false;
            }

            return true;

        }
        return false;

    }


    @Override
    public String label() {
        return "RongShu_TransferData_ArtificialAndCustomerBatch_To_Delay";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.BATCH_MESSAGE_DELAY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.RS_DATA_COLLECTION.getCode();
    }

    public static void main(String[] args) {
        LocalDate localDate = LocalDate.now().minusDays(87);
        System.out.println(localDate);
    }
}
