package com.br.marketing.api.entities.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.common.utils.StringUtils;

/** 规则引擎返回结果操作类
 * @author Wang Weiwei
 * @since 2018/3/16
 */
public class LoanRetrialResult extends Result {

    /**
     * Instantiates a new Loan retrial result.
     */
    public LoanRetrialResult() {
        super();
    }

    /**
     * Instantiates a new Loan retrial result.
     *
     * @param obj the obj
     */
    public LoanRetrialResult(JSONObject obj) {
        super(obj);
    }

    /**
     * Instantiates a new Loan retrial result.
     *
     * @param json the json
     */
    public LoanRetrialResult(String json) {
        super(json);
    }

    /**
     * Set retry response.
     *
     * @param json the json
     */
    public void setRetryResponse(String json){
        if(StringUtils.isEmpty(json)){
            this.data.put("retryResponse",new JSONObject());
        }else {
            this.data.put("retryResponse", JSON.parseObject(json));
        }
    }

    /**
     * Set examine history.
     *
     * @param json the json
     */
    public void setExamineHistory(String json){
        if(StringUtils.isEmpty(json)){
            this.data.put("examineResponse",new JSONObject());
        }else {
            this.data.put("examineResponse", JSON.parseObject(json));
        }
    }

    /**
     * Get retry response json object.
     *
     * @return the json object
     */
    public JSONObject getRetryResponse(){
        return this.data.getJSONObject("retryResponse");
    }

    /**
     * Get examine history json object.
     *
     * @return the json object
     */
    public JSONObject getExamineHistory(){
        return this.data.getJSONObject("examineResponse");
    }

    /**
     * Set examine result.
     *
     * @param value the value
     */
    public void setExamineResult(String value){
        setDataString("examineResult",value);
    }

    /**
     * Get examine result string.
     *
     * @return the string
     */
    public String getExamineResult(){
        return getDataString("examineResult");
    }

    /**
     * Set retry result.
     *
     * @param value the value
     */
    public void setRetryResult(String value){
        setDataString("retryResult",value);
    }

    /**
     * Get retry result string.
     *
     * @return the string
     */
    public String getRetryResult(){
        return getDataString("retryResult");
    }

    /**
     * Set retry risk.
     *
     * @param value the value
     */
    public void setRetryRisk(String value){
        setDataString("retryRisk",value);
    }

    /**
     * Get retry risk string.
     *
     * @return the string
     */
    public String getRetryRisk(){
        return getDataString("retryRisk");
    }

    /**
     * Set success.
     *
     * @param flag the flag
     */
    public void setSuccess(int flag){
        this.data.put("success",flag);
    }

    /**
     * Is success boolean.
     *
     * @return the boolean
     */
    public boolean isSuccess(){
        if(this.getData().getInteger("success")!=null){
            return this.getData().getInteger("success")==1;
        }
        return false;
    }

}
