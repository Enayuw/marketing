package com.br.marketing.rule.rongshu;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.RsxkCollectDataImpl;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * @Author: dongshuo.he
 * @Date: 2025/3/26 15:27
 * D20250314榕树新客自运营ab意向自动化转Daas-4004739/4004713-静置规则
 * https://c.100credit.cn/pages/viewpage.action?pageId=201076403
 */
@Service
@Slf4j
public class RsxkCallRecordToDassToDelayImpl implements AssembleData<MqFact>{

    @Resource
    PushDataService pushDataService;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO callRecordBO = (CallRecordBO) transmitFact;
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(callRecordBO.getId());
        mqFact.setSource(TransferSource.CUSTOMER_CALL_RECORD.getCode());
        Set set = new HashSet<>();
        set.add("Rsxk_CallRecordData_ToDaas");
        mqFact.setIncludeRules(set);
        return mqFact;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if(transmitFact instanceof CallRecordBO){
            CallRecordBO bo = (CallRecordBO) transmitFact;
            if (!pushDataService.isPushDassWithCallGrade(label(), bo.getDetail().getIntentionGrade())) {
                return false;
            }
            RsxkCollectDataImpl.RsxkRuleNecessaryData ruleNecessaryData = 
                    (RsxkCollectDataImpl.RsxkRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, SyncUserValidityPeriodBO> syncUserPeriodMap = ruleNecessaryData.getSyncUserPeriodMap();
            SyncUserValidityPeriodBO syncUserData = syncUserPeriodMap.get(bo.getCaseNum());
            if (syncUserData == null) {
                return false;
            }
            JSONObject userTypeConfig = marketingCommonConfig.getRsxkToDassUserTypeConfig();
            JSONObject configForApiCode = userTypeConfig.getJSONObject(bo.getApiCode());
            return configForApiCode.containsKey(syncUserData.getSyncUser().getUserType());
        }
        return false;
    }

    @Override
    public String label() {
        return "Rsxk_CallRecordData_ToDaas_Delay";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CALLRECORD_MESSAGE_DELAY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.RSXK_DATA_COLLECTION.getCode();
    }
}
