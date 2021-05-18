package com.br.marketing.api.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;
import com.br.marketing.api.entity.RiskRank;

import java.util.concurrent.Future;

/** 风险分级计算执行器
 * @author Wang Weiwei
 * @since 2018/3/20
 */
public class RiskRankInvoker extends BaseRetryTaskInvoker<StrategyResult> {
    private StrategyResult strategyResult;
    private StrategyApiContext strategyApiContext;

    /**
     * 风险分级计算执行器
     * @param strategyApiContext 上下文
     * @param strategyResult 策略结果
     */
    public RiskRankInvoker(StrategyApiContext strategyApiContext, StrategyResult strategyResult) {
        super();
        this.strategyApiContext = strategyApiContext;
        this.strategyResult = strategyResult;
    }

    @Override
    Future<StrategyResult> retryInvoker() {
        RiskRank riskRank = strategyApiContext.getRiskRank();
        //1、计算规则集风险分级
        //2、计算全局的风险等级
        JSONObject loanStrategy = strategyResult.getLoanStrategy();
        JSONArray ruleArray = strategyResult.getRuleArray();
        //规则集的风险等级
        String maxRiskRank = "A";
        if (ruleArray.size() > 0){
            for (int i=0;i<ruleArray.size();i++){
                JSONObject jsonObject = ruleArray.getJSONObject(i);
                Integer ruleWeight = jsonObject.getInteger("ruleWeight");
                String riskRuleType = riskRank.getRiskRank(ruleWeight);
                jsonObject.put("rulerisk",riskRuleType);
                if (maxRiskRank.compareTo(riskRuleType)  < 0){
                    maxRiskRank = riskRuleType;
                }
            }
        }else {
            maxRiskRank = "无结果";
        }

        //获取规则集最严重的风险等级
        loanStrategy.put("ruleFinalRisk",maxRiskRank);
        return null;
    }
}
