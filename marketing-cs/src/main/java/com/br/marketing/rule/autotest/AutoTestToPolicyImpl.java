package com.br.marketing.rule.autotest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
public class AutoTestToPolicyImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {
    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
        String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
        String name = BrCipherMaker.getInstance().decode(syncUser.getName());
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
        pushMarketingUserDetailByRuleDTO.setInitId(syncUser.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(syncUser.getCustNum());
        String phone = syncUser.getCellMd5();
        pushMarketingUserDetailByRuleDTO.setPhone(phone);
        pushMarketingUserDetailByRuleDTO.setCell(cell);
        JSONObject varDto = new JSONObject();
        varDto.put("id", syncUser.getIdCard());
        varDto.put("name", name);
        for (String key : jsonObject.keySet()) {
            varDto.put(key, jsonObject.get(key));
        }
        pushMarketingUserDetailByRuleDTO.setBatchNumber(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_"
                + context.getApiCode());
        pushMarketingUserDetailByRuleDTO.setVariables(varDto);
        pushMarketingUserDetailByRuleDTO.setStrategyCode("");
        return pushMarketingUserDetailByRuleDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
       return true;
    }

    @Override
    public String label() {
        return "Auto_test_to_policy";
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
