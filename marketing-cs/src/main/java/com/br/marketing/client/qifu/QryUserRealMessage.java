package com.br.marketing.client.qifu;


public class QryUserRealMessage {

    /**
     * 手机号
     */
    private String mobileMd5;
    /**
     * 营销信号:Y 停止营销 N 可营销
     */
    private String stopMarketingSign;
    /**
     * 交易信息
     */
    private Object tradeMessageRes;
    /**
     * 唯一号
     */
    private String uniqueReqNo;
    /**
     * 授信信息
     */
    private Object riskMessageRes;
    /**
     * 用户完件信息
     */
    private Object userMessageRes;

    public String getMobileMd5() {
        return mobileMd5;
    }

    public void setMobileMd5(String mobileMd5) {
        this.mobileMd5 = mobileMd5;
    }

    public String getStopMarketingSign() {
        return stopMarketingSign;
    }

    public void setStopMarketingSign(String stopMarketingSign) {
        this.stopMarketingSign = stopMarketingSign;
    }

    public Object getTradeMessageRes() {
        return tradeMessageRes;
    }

    public void setTradeMessageRes(Object tradeMessageRes) {
        this.tradeMessageRes = tradeMessageRes;
    }

    public String getUniqueReqNo() {
        return uniqueReqNo;
    }

    public void setUniqueReqNo(String uniqueReqNo) {
        this.uniqueReqNo = uniqueReqNo;
    }

    public Object getRiskMessageRes() {
        return riskMessageRes;
    }

    public void setRiskMessageRes(Object riskMessageRes) {
        this.riskMessageRes = riskMessageRes;
    }

    public Object getUserMessageRes() {
        return userMessageRes;
    }

    public void setUserMessageRes(Object userMessageRes) {
        this.userMessageRes = userMessageRes;
    }

}