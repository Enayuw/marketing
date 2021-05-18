package com.br.marketing.api.entities.client;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyResult;

/**
 * The type Rule task result.
 */
public class RuleTaskResult extends Result {

    /**
     * Instantiates a new Rule task result.
     *
     * @param object the object
     */
    public RuleTaskResult(JSONObject object){
        super(object);
    }

    /**
     * Set rule result.
     *
     * @param ruleResult the rule result
     */
    public void setRuleResult(RuleResult ruleResult){
        this.data.put("ruleResult",ruleResult);
    }

    /**
     * Set strategy result.
     *
     * @param strategyResult the strategy result
     */
    public void setStrategyResult(StrategyResult strategyResult){
        this.data.put("strategyResult",strategyResult);
    }

    /**
     * Set hx result.
     *
     * @param hxResult the hx result
     */
    public void setHxResult(HxResult hxResult){
        this.data.put("hxResult",hxResult);
    }

    /**
     * Get rule result rule result.
     *
     * @return the rule result
     */
    public RuleResult getRuleResult(){
        return new RuleResult(this.data.getJSONObject("ruleResult"));
    }

    /**
     * Get strategy result strategy result.
     *
     * @return the strategy result
     */
    public StrategyResult getStrategyResult(){
        return new StrategyResult(this.data.getString("strategyResult"));
    }

    /**
     * Get hx result hx result.
     *
     * @return the hx result
     */
    public HxResult getHxResult(){
        if(this.data.containsKey("hxResult")){
            return new HxResult(this.data.getString("hxResult"));
        }else{
            return null;
        }
    }

}
