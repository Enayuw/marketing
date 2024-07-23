package com.br.marketing.rule.ibu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;


@Service
public class InitDataToPolicyImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    PushRuleService pushRuleService;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
        Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
        if (pushCellEncPolicy != null && pushCellEncPolicy.get(context.getApiCode()) != null) {
            encType = pushCellEncPolicy.get(context.getApiCode());
        }
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
        pushMarketingUserDetailByRuleDTO.setInitId(syncUser.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(syncUser.getCustNum());
        pushMarketingUserDetailByRuleDTO.setPhone(pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getCell())));
        JSONObject varDto = new JSONObject();
        varDto.put("groupType", syncUser.getUserType());
        varDto.put("id", pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getIdCard())));
        varDto.put("name", pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getName())));
        varDto.put("cusName", pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getName())));
        String reserveField1 = syncUser.getReserveField1();
        if (StringUtils.isNotBlank(reserveField1)) {
            JSONObject initJson = JSON.parseObject(reserveField1);
            for (String s : initJson.keySet()) {
                varDto.put(s, initJson.getString(s));
                if (s.toLowerCase().equals("strategycode")) {
                    pushMarketingUserDetailByRuleDTO.setStrategyCode(initJson.getString(s));
                }
                if (s.toLowerCase().equals("batchnumber")) {
                    pushMarketingUserDetailByRuleDTO.setBatchNumber(initJson.getString(s));
                }
                if("repayTime".equals(s)){
                    varDto.put("deadlineday",initJson.getString(s));
                }
            }
        }
        if (StringUtils.isBlank(pushMarketingUserDetailByRuleDTO.getStrategyCode())) {
            pushMarketingUserDetailByRuleDTO.setStrategyCode("");
        }
        pushMarketingUserDetailByRuleDTO.setVariables(varDto);
        return pushMarketingUserDetailByRuleDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MqFact contextMqFact = context.getMqFact();
        if (!TransferSource.INIT_DATA_SET_PROCESS.getCode().equals(contextMqFact.getSource())) {
            return false;
        }
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        String reserveField1 = syncUser.getReserveField1();
        if (StringUtils.isBlank(reserveField1)) {
            return false;
        }
        JSONObject jsonObject = JSON.parseObject(reserveField1);
        String batchNumber = jsonObject.getString("batchNumber");
        if (StringUtils.isBlank(batchNumber)) {
            return false;
        }
        String operateType = jsonObject.getString("operateType");
        if (StringUtils.isNotBlank(operateType) && operateType.equals("1")) {
            return true;
        }
        return false;
    }

    @Override
    public String label() {
        return "InitData_To_Policy_Rule";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.INIT_TO_POLICY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

}
