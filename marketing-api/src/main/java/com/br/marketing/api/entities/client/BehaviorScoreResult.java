package com.br.marketing.api.entities.client;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.api.Result;

/** 规则引擎返回结果操作类
 * @author Wang Weiwei
 * @since 2018/3/16
 */
public class BehaviorScoreResult extends Result {

    /**
     * Instantiates a new Behavior score result.
     */
    public BehaviorScoreResult() {
        super();
    }

    /**
     * Instantiates a new Behavior score result.
     *
     * @param obj the obj
     */
    public BehaviorScoreResult(JSONObject obj) {
        super(obj);
    }

    /**
     * Instantiates a new Behavior score result.
     *
     * @param json the json
     */
    public BehaviorScoreResult(String json) {
        super(json);
    }

    /**
     * Set behavior risk.
     *
     * @param risk the risk
     */
    public void setBehaviorRisk(String risk){
        this.getData().put("behaviorRisk",risk);
    }

    /**
     * Get behavior risk string.
     *
     * @return the string
     */
    public String getBehaviorRisk(){
        return this.getDataString("behaviorRisk");
    }

    /**
     * Set success.
     *
     * @param flag the flag
     */
    public void setSuccess(int flag){
        this.getData().put("success",flag);
    }

    /**
     * Is success boolean.
     *
     * @return the boolean
     */
    public Boolean isSuccess(){
        if(this.getData().getInteger("success")!=null){
            return this.getData().getInteger("success") == 1;
        }
        return Boolean.FALSE;
    }
}
