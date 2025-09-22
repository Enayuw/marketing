package com.br.marketing.rule.ai.go;

import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.ai.strategy.AiToPolicyStrategyFactory;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI推决策规则空壳类
 * 实现AssembleData接口，可以被Spring注册
 * 实际业务逻辑委托给AiToPolicyBase处理
 * 
 * @author AI Assistant
 * @date 2024
 */
@Service
@Slf4j
public class AiToPolicyRule implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Autowired
    protected AiToPolicyStrategyFactory strategyFactory;


    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        String operateType = syncUser.getOperateType();
        AiToPolicyBase aiToPolicyBase = (AiToPolicyBase) strategyFactory.getStrategy(operateType);
        return aiToPolicyBase.assemble(transmitFact, context);
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingSyncUser) {
            MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
            String operateType = syncUser.getOperateType();
            // todo 增加判断speed
            AiToPolicyBase aiToPolicyBase = (AiToPolicyBase) strategyFactory.getStrategy(operateType);
            return aiToPolicyBase.insertRecord(syncUser);
        }
        return false;
    }

    @Override
    public String label() {
        return "AI_To_Policy";
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