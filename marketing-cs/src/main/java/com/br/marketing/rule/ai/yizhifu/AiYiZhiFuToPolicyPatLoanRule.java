package com.br.marketing.rule.ai.yizhifu;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AiYiZhiFuToPolicyPatLoanRule implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        PushMarketingUserDetailByRuleDTO pushData = new PushMarketingUserDetailByRuleDTO();
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;

        pushData.setInitId(syncUser.getId());
        pushData.setCaseNumber(syncUser.getCustNum());
        pushData.setPhone(syncUser.getCell());

        String apiCode = syncUser.getApiCode();
        String appletDate = syncUser.getAppletDate().replace("-", "");
        String reserveField1 = syncUser.getReserveField1();

        JSONObject jsonObject = Optional.ofNullable(reserveField1)
                .filter(StringUtils::isNotBlank)
                .map(JSONObject::parseObject)
                .orElseGet(JSONObject::new);

        if (ObjectUtil.isNotEmpty(jsonObject)) {
            String batchNumber = ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                    ? jsonObject.getString("batchNumber")
                    : (appletDate + "_" + apiCode);
            String strategyCodeOriginal = ObjectUtil.isNotEmpty(jsonObject.getString("strategyCode"))
                    ? jsonObject.getString("strategyCode")
                    : "";
            String strategyCode = strategyCodeOriginal.length() < 12
                    ? strategyCodeOriginal
                    : strategyCodeOriginal.substring(strategyCodeOriginal.length() - 12);
            jsonObject.put("strategyCode", strategyCode);
            String userType  = strategyCodeOriginal.length() <= 12
                    ? emptyDefault(syncUser.getUserType())
                    : strategyCodeOriginal.substring(0, strategyCodeOriginal.length() - 12);
            String batchName = ObjectUtil.isNotEmpty(jsonObject.getString("batchName"))
                    ? jsonObject.getString("batchName")
                    : (appletDate + "_" + apiCode);
            String strategyName = ObjectUtil.isNotEmpty(jsonObject.getString("strategyName"))
                    ? jsonObject.getString("strategyName")
                    : "";

            pushData.setStrategyCode(strategyCode);
            pushData.setBatchNumber(batchNumber);
            pushData.setBatchName(batchName);

            jsonObject.put("strategyCode", strategyCode);
            jsonObject.put("strategyName", strategyName);
            jsonObject.put("batchName", batchName);

            if (StringUtils.isNotEmpty(userType)) {
                jsonObject.put("userType", userType);
            }

            Optional.ofNullable(jsonObject.get("sex"))
                    .ifPresent(sex -> {
                        if ("0".equals(sex)) {
                            jsonObject.put("gender", "男");
                        } else if ("1".equals(sex)) {
                            jsonObject.put("gender", "女");
                        }
                    });
        }

        buildJson(jsonObject, syncUser);
        pushData.setVariables(jsonObject);
        return pushData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return Optional.ofNullable(transmitFact)
                .filter(MarketingSyncUser.class::isInstance)
                .map(MarketingSyncUser.class::cast)
                .map(MarketingSyncUser::getOperateType)
                .filter("3"::equals)
                .isPresent();
    }

    @Override
    public String label() {
        return "AI_YIZHIFU_To_Policy_PatLoan";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.INIT_TO_POLICY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

    private JSONObject buildJson(JSONObject jsonObject, MarketingSyncUser syncUser) {
        jsonObject.put("cusBatch", emptyDefault(syncUser.getCusBatch()));
        jsonObject.put("requestBatch", emptyDefault(syncUser.getRequestBatch()));
        jsonObject.put("custNum", emptyDefault(syncUser.getCustNum()));
        jsonObject.put("idCard", emptyDefault(syncUser.getIdCard()));
        jsonObject.put("name", emptyDefault(syncUser.getName()));
        jsonObject.put("groupType", emptyDefault(syncUser.getGroupType()));
        jsonObject.put("operateType", emptyDefault(syncUser.getOperateType()));
        jsonObject.put("registerDate", emptyDefault(syncUser.getRegisterDate()));
        jsonObject.put("appletDate", emptyDefault(syncUser.getAppletDate()));
        jsonObject.put("taskId", emptyDefault(syncUser.getCusBatch()));
        return jsonObject;
    }

    private String emptyDefault(String value) {
        return StringUtils.defaultIfEmpty(value, "");
    }
}