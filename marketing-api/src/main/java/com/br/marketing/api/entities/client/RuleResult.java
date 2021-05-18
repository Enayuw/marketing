package com.br.marketing.api.entities.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.api.Result;

/** 规则引擎返回结果操作类
 * @author Wang Weiwei
 * @since 2018/3/16
 */
public class RuleResult extends Result {
    /**
     * Instantiates a new Rule result.
     */
    public RuleResult() {
        super();
    }

    /**
     * Instantiates a new Rule result.
     *
     * @param obj the obj
     */
    public RuleResult(JSONObject obj) {
        super(obj);
    }

    /**
     * Instantiates a new Rule result.
     *
     * @param json the json
     */
    public RuleResult(String json) {
        super(json);
    }


    /**
     * Gets rule list.
     *
     * @return the rule list
     */
    public JSONArray getRuleList() {
        return getDefaultArray("ruleList", new JSONArray());
    }
}
