package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * D20231218数禾电销数据自动化转决策
 * https://c.100credit.cn/pages/viewpage.action?pageId=141601244
 * @author chenh
 */
@Service
@Slf4j
public class ShuHeSyncDataToPolicyImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
        pushMarketingUserDetailByRuleDTO.setInitId(syncUser.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(syncUser.getCustNum());
        // 手机号log解密  md5加密
        String cell = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(syncUser.getCell()).getBytes());
        pushMarketingUserDetailByRuleDTO.setPhone(cell);

        String apiCode = syncUser.getApiCode();
        pushMarketingUserDetailByRuleDTO.setBatchNumber(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + apiCode);

        String strategyCode = marketingCommonConfig.getShuheToJueCeStrategy().get(apiCode);
        pushMarketingUserDetailByRuleDTO.setStrategyCode(strategyCode);

        JSONObject varDto = new JSONObject();
        varDto.put("userType", syncUser.getUserType());
        pushMarketingUserDetailByRuleDTO.setVariables(varDto);

        log.warn("数禾上传数据推送决策,apicode={}", apiCode);
        return pushMarketingUserDetailByRuleDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingSyncUser) {
            return true;
        }

        return false;
    }

    @Override
    public String label() {
        return "ShuHe_SyncData_Policy";
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
