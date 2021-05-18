package com.br.marketing.api.entities.api;

import com.alibaba.fastjson.JSONObject;

/** 策略贷中用户参数对象
 * @author Wang Weiwei
 * @since 2018/3/12
 */
public class ApiUserParam extends PutParam {
    /**
     * Gets swift strategy result.
     *
     * @return the swift strategy result
     */
    public StrategyResult getSwiftStrategyResult() {
        return swiftStrategyResult;
    }

    /**
     * Sets swift strategy result.
     *
     * @param swiftStrategyResult the swift strategy result
     */
    public void setSwiftStrategyResult(StrategyResult swiftStrategyResult) {
        this.swiftStrategyResult = swiftStrategyResult;
    }

    private StrategyResult swiftStrategyResult;

    /**
     * Instantiates a new Api user param.
     *
     * @param strData the str data
     */
    public ApiUserParam(String strData) {
        super(strData);
    }

    /**
     * Instantiates a new Api user param.
     *
     * @param jsonObject the json object
     */
    public ApiUserParam(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Gets default id.
     *
     * @param i the
     * @return the default id
     */
    public String getDefaultId(int i) {
        return getDefaultString("id",(i + 1) + "");
    }

    /**
     * Get approve result string.
     *
     * @return the string
     */
    public String getApproveResult(){
        return getDataString("approveResult");
    }

    /**
     * 获取
     * @return
     */
    public String getUserDate(){
        return getDataString("userDate");
    }
    /**
     * 获取
     * @return
     */
    public String getDecodeFailType(){
        return getDataString("decodeFailType");
    }

    /**
     * Get sl user date string.
     *
     * @return the string
     */
    public String getSlUserDate(){
        return getDataString("slUserDate");
    }

    /**
     * Get user time string.
     *
     * @return the string
     */
    public String getUserTime(){
        return getDataString("userTime");
    }

    /*public void setUserDate(String userDate){
        data.put("userDate",userDate);
    }*/

    /**
     * 获取身份证号
     * */
    public String getIdCard() {
        return getDataString("idCard");
    }

    /**
     * Sets id card.
     *
     * @param idCard the id card
     */
    public void setIdCard(String idCard) {
        data.put("idCard",idCard);
    }

    /**
     * Get batch id string.
     *
     * @return the string
     */
    public String getBatchId(){
        return getDataString("batchId");
    }

    /**
     * Set batch id.
     *
     * @param batchId the batch id
     */
    public void setBatchId(String batchId){
        data.put("batchId",batchId);
    }
    /**
     * 获取策略编号
     */
    public String getStrategyId() {
        return  getDataString("strategyId");
    }

    /**
     * Sets strategy id.
     *
     * @param strategyId the strategy id
     */
    public void setStrategyId(String strategyId) {
        setDataString("strategyId",strategyId);
    }
    /**
     * 获取手机号
     * */
    public String getPhone() {
        return  getDataString("cell");
    }

    /**
     * Sets phone.
     *
     * @param cell the cell
     */
    public void setPhone(String cell) {
        setDataString("cell",cell);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return  getDataString("name");
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        setDataString("name",name);
    }

    /**
     * Get cus num string.
     *
     * @return the string
     */
    public String getCusNum(){
        return  getDataString("cusNum");
    }

    /**
     * Get original id string.
     *
     * @return the string
     */
    public String getOriginalId(){
        return getDataString("originalId");
    }

    /**
     * Set original id.
     *
     * @param value the value
     */
    public void setOriginalId(String value){
        setDataString("originalId",value);
    }

    /**
     * Get original cell string.
     *
     * @return the string
     */
    public String getOriginalCell(){
        return getDataString("originalCell");
    }

    /**
     * Set original cell.
     *
     * @param value the value
     */
    public void setOriginalCell(String value){
        setDataString("originalCell",value);
    }

    /**
     * 获取审批通过日
     * */
    public String getPassDate() {
        return  getDataString("passDate");
    }


    /**
     * 获取贷款到期日
     * */
    public String getMaturityDate() {
        return  getDataString("loanMaturityDate");
    }

    /**
     * 获取被监控人编号
     * */
    public String getPersonId() {
        return  getDataString("id");
    }

    /**
     * 获取联系人
     * @return
     */
    public String getLinkmen(){
        return getDataString("linkmen");
    }

}
