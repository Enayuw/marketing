package com.br.marketing.api.entities.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.JsonData;
import com.br.marketing.common.constants.web.ResponseCode;
import com.br.marketing.common.utils.transaction.SwiftNumberManager;

/** API接口返回结果
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/12/31
 */
public class Result extends JsonData {
    /**
     * 在对象中包裹的，无flag，code等值的Result
     * */
    public Result(){
        data = new JSONObject();
        data.put("swift_number", SwiftNumberManager.generate());
        data.put("code","00");
        data.put("message", "成功");
    }

    /**
     * Put flag.
     *
     * @param flag the flag
     */
    public void putFlag(JSONObject flag) {
        getData().put("flag", flag);
    }

    /**
     * Sets hx result.
     *
     * @param hxResult the hx result
     */
    public void setHxResult(JSONObject hxResult) {
        if(hxResult!=null){
            this.getData().put("hxResult", hxResult);
        }
    }

    /**
     * Sets json data.
     *
     * @param jsonData the json data
     */
    public void setJsonData(JSONObject jsonData) {
        if(jsonData!=null){
            this.getData().put("jsonData",jsonData);
        }
    }

    /**
     * 以外部传送的json作为data值
     * @param obj 存储数据
     */
    public Result(JSONObject obj){
        this.data = obj;
    }


    /**
     * 根据外部传入的json字符串
     * @param json json字符串
     */
    public Result(String json) {
        data = JSONObject.parseObject(json);
    }


    /**
     * 设置结果的返回码
     * @param code ResponseCode 枚举实例
     * */
    public void putCode(ResponseCode code){
        data.put("code", code.getCode());
        data.put("message", code.getMessage());
    }

    /**
     * Add hx result.
     *
     * @param value the value
     */
    public void addHxResult(Object value){
        data.put("hxResult",value);
    }

    @Override
    public void setSwiftNumber(String swiftNumber){
        data.put("swift_number", swiftNumber);
    }


    /**
     * 批量结果中添加某结果
     * @param cusNum 客户编号
     * @param strategyResult 策略结果
     */
    public void addStrategyResult(String cusNum, StrategyResult strategyResult) {
        JSONObject strategy = new JSONObject();
        strategy.put("cusNum", cusNum);
        strategy.put("result", strategyResult.getData());
        getResultArray().add(strategy);
    }

    /**
     * Gets result array.
     *
     * @return the result array
     */
    public JSONArray getResultArray() {
        return getDefaultArray("ResultArray", new JSONArray());
    }

    /**
     * Remove result array.
     */
    public void removeResultArray() {
        data.remove("ResultArray");
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public String getCode() {
        return getDataString("code");
    }

    /**
     * New data.
     */
    public void newData() {
        this.data = new JSONObject();
    }

    /**
     * Error array to single.
     */
    public void errorArrayToSingle() {
        JSONArray errorArray = getDefaultArray("errorArray", new JSONArray());
        if (errorArray.size() > 0){
            getData().putAll(errorArray.getJSONObject(0).getJSONArray("error").getJSONObject(0));
            getData().remove("id");
        }
        getData().remove("errorArray");
    }
}
