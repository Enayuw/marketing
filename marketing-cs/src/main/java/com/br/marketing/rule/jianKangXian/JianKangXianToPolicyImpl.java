package com.br.marketing.rule.jianKangXian;

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
public class JianKangXianToPolicyImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {
    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
        String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
        String cell4 = cell.substring(cell.length() - 4);
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
        varDto.put("cell4",cell4);
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
        if (transmitFact instanceof MarketingSyncUser) {
            MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
            String reserveField1 = syncUser.getReserveField1();
            if (StringUtils.isBlank(reserveField1)) {
                return false;
            }
            JSONObject jsonObject = JSON.parseObject(reserveField1);
            String operateType = jsonObject.getString("operateType");
            if (StringUtils.isNotBlank(operateType) && "1".equals(operateType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "HealthData_To_Policy_Rule";
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
