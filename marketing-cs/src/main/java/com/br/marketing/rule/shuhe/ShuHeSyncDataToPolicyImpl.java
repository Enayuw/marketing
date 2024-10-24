package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

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
        // 手机号log解密  md5加密
        String cell = pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getCell()));
        pushMarketingUserDetailByRuleDTO.setPhone(cell);

        String apiCode = syncUser.getApiCode();
        pushMarketingUserDetailByRuleDTO.setBatchNumber(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + apiCode);

        String strategyCode = marketingCommonConfig.getShuheToJueCeStrategy().get(apiCode);
        pushMarketingUserDetailByRuleDTO.setStrategyCode(strategyCode);

        JSONObject varDto = new JSONObject();
        JSONObject parseObject = JSON.parseObject(syncUser.getReserveField1());
        String groupTypeNew = parseObject.getOrDefault("groupTypeNew", "").toString();
        if (StringUtils.isNotBlank(groupTypeNew)) {
            varDto.put("groupType", groupTypeNew);
        }

        HashMap<String, String> dataCleanMappingMap = marketingCommonConfig.getDataCleanMappingMap();
        String value = dataCleanMappingMap.get(apiCode);
        if (value != null) {
            List<String> dataCleanValue = marketingCommonConfig.getDataCleanValue();
            String customNameType = dataCleanValue != null ? dataCleanValue.get(0) : "customNameType";
            varDto.put(customNameType, parseObject.getOrDefault(value, "").toString());
        }
        varDto.put("orderId", syncUser.getCustNum());
        varDto.putAll(parseObject);
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
