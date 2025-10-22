package com.br.marketing.rule.ai.policy;

import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.common.CommonRuleLabelEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI推决策规则空壳类
 * 实现AssembleData接口，可以被Spring注册
 * 实际业务逻辑委托给AbstractBaseAiToPolicy处理
 */
@Service
@Slf4j
public class AiToPolicyFacade implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Autowired
    protected AiToPolicyProcessorFactory strategyFactory;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        String operateType = syncUser.getOperateType();
        AbstractBaseAiToPolicy abstractBaseAiToPolicy = (AbstractBaseAiToPolicy) strategyFactory.getStrategy(operateType);
        return abstractBaseAiToPolicy.assembleData(transmitFact, context);
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingSyncUser) {
            MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
            String operateType = syncUser.getOperateType();
            if (!marketingCommonConfig.getAiToPolicyOperateTypeList().contains(operateType)) {
                return false;
            }
            AbstractBaseAiToPolicy aiToPolicyBase = (AbstractBaseAiToPolicy) strategyFactory.getStrategy(operateType);
            return aiToPolicyBase.insertRecord(syncUser);
        }

        return false;
    }

    @Override
    public String label() {
        return CommonRuleLabelEnum.AI_TO_POLICY.getCode();
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