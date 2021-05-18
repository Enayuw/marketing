package com.br.marketing.api.entities.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.client.BehaviorScoreResult;
import com.br.marketing.api.entities.client.HxResult;
import com.br.marketing.api.entities.client.LoanRetrialResult;
import com.br.marketing.api.entities.client.SanxiangzhiliResult;
import com.br.marketing.common.utils.Constants;

/** 策略单条返回结果
 * @author Wang Weiwei
 * @since 2018/3/16
 */
public class StrategyResult extends Result{
    /**
     * Instantiates a new Strategy result.
     */
    public StrategyResult() {
        super();
    }

    /**
     * Instantiates a new Strategy result.
     *
     * @param obj the obj
     */
    public StrategyResult(JSONObject obj) {
        super(obj);
    }

    /**
     * Instantiates a new Strategy result.
     *
     * @param json the json
     */
    public StrategyResult(String json) {
        super(json);
    }

    /**
     * Sets rule array.
     *
     * @param ruleArray the rule array
     */
    public void setRuleArray(JSONArray ruleArray) {
        JSONObject loanS = getLoanStrategy();
        loanS.put("ruleArray", ruleArray);
    }


    /**
     *1. 经过规则结果转换后的数据格式样例
     * [
     *  {
     *      "ruleType":"RuleSpecialList", // 规则集编号
     *      "version":"1.0",
     *      "ruleWeight":80, // 规则集权重
     *      "loanRule" : [
     *          {
     *              "ruleCode" : "QJS020",
     *              "ruleName": "直系亲属银行不良",
     *              "weight":80, //规则权重
     *              "ruleKeys": ["1","1","1"]  // 规则变量值
     *          }
     *      ]
     *  }
     * ]
     *
     * */
    public JSONArray getRuleArray(){
        JSONObject loanS = getLoanStrategy();
        if (loanS.containsKey("ruleArray")){
            return loanS.getJSONArray("ruleArray");
        }else {
            JSONArray array = new JSONArray();
            loanS.put("ruleArray", array);
            return array;
        }
    }


    /**
     * 获取贷中策略返回结果
     * */
    public JSONObject getLoanStrategy(){
        return getDefaultObject("loanStrategy", new JSONObject());
    }


    /**
     * 获取策略最终建议
     * */
    public String getAdvice() {
        return "";
    }

    /**
     * 获取最终风险等级
     * */
    public String getRiskLevel() {
        return getLoanStrategy().getString("strategyDecision");
    }

    /**
     * Get rule risk string.
     *
     * @return the string
     */
    public String getRuleRisk(){
        return getLoanStrategy().getString("ruleFinalRisk");
    }

    /**
     * Get retry risk string.
     *
     * @return the string
     */
    public String getRetryRisk(){
        return getLoanStrategy().getString("retryRisk");
    }

    /**
     * Get behavior risk string.
     *
     * @return the string
     */
    public String getBehaviorRisk(){
        return getLoanStrategy().getString("behaviorRisk");
    }

    /**
     * Gets flag.
     *
     * @return the flag
     */
    public JSONObject getFlag() {
        return getDefaultObject("flag", new JSONObject());
    }

    /**
     * Add flag.
     *
     * @param key   the key
     * @param value the value
     */
    public void addFlag(String key, Object value) {
        getFlag().put(key, value);
    }

    /**
     * Add error flag.
     */
    public void addErrorFlag() {
        addFlag("loanStrategy", "99");
    }

    /**
     * Remove swift number.
     */
    public void removeSwiftNumber() {
        data.remove("swiftNumber");
    }

    @Override
    public String getCode() {
        return getDataString("code");
    }

    /**
     * Remove code.
     */
    public void removeCode() {
        getData().remove("code");
        getData().remove("message");
    }

    @Override
    public void putFlag(JSONObject flag) {
        getData().put("flag", flag);
    }

    @Override
    public void setHxResult(JSONObject hxResult) {
        if(hxResult!=null){
            this.getData().put("hxResult", hxResult);
        }
    }

    @Override
    public void setJsonData(JSONObject jsonData) {
        if(jsonData!=null){
            this.getData().put("jsonData",jsonData);
        }
    }

    /**
     * Sets sanxiangzhili result.
     *
     * @param sanxiangzhiliResult the sanxiangzhili result
     */
    public void setSanxiangzhiliResult(SanxiangzhiliResult sanxiangzhiliResult) {
        if(sanxiangzhiliResult!=null){
            sanxiangzhiliResult.removeOthers();
            this.getData().put("sanxiangzhiliResult", sanxiangzhiliResult.getData());
        }
    }

    /**
     * Set score result.
     *
     * @param scoreResult the score result
     * @param behavior    the behavior
     */
    public void setScoreResult(BehaviorScoreResult scoreResult, JSONObject behavior){
        JSONObject result = new JSONObject();
        result.put("final_decision",scoreResult.getBehaviorRisk());
        result.put("pro_name",behavior.getString("pro_name"));
        result.put("pro_code",behavior.getString("pro_code"));
        result.put("version",behavior.getString("version"));
        result.put("score",scoreResult.getData().getJSONObject("Score"));
        this.getLoanStrategy().put("scoreResult", result);
    }

    /**
     * Set version.
     *
     * @param value the value
     */
    public void setVersion(String value){
        this.getLoanStrategy().put("version",value);
    }

/*    public void setRetryResult(LoanRetrialResult retryResult){
        JSONObject result = new JSONObject();
        JSONObject object = retryResult.getRetryResponse();
        if(object!=null){
            boolean flag = false;
            for(String key:object.keySet()){
                JSONObject newItem = new JSONObject();
                JSONObject item = object.getJSONObject(key);
                if(item!=null && ("00".equals(item.getString("code")) || "100002".equals(item.getString("code")))){
                    JSONObject riskStrategy = item.getJSONObject("RiskStrategy");
                    newItem.putAll(riskStrategy);
                    newItem.remove("Rule");
                    newItem.remove("Score");
                    newItem.remove("strategy_id");
                    result.put(key,newItem);
                    flag = true;
                }
            }
            if(flag){
                this.getLoanStrategy().put("reviewStrA", result);
                this.getLoanStrategy().put("approveResult", Constants.examineMap.get(retryResult.getExamineResult()));
            }
        }

    }*/
}
