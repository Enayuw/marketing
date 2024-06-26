package com.br.marketing.client.qifu;

import java.util.List;

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
    private List<TradeMessageRes> tradeMessageRes;
    /**
     * 唯一号
     */
    private String uniqueReqNo;
    /**
     * 授信信息
     */
    private List<RiskMessageRes> riskMessageRes;
    /**
     * 用户完件信息
     */
    private List<UserMessageRes> userMessageRes;

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

    public List<TradeMessageRes> getTradeMessageRes() {
        return tradeMessageRes;
    }

    public void setTradeMessageRes(List<TradeMessageRes> tradeMessageRes) {
        this.tradeMessageRes = tradeMessageRes;
    }

    public String getUniqueReqNo() {
        return uniqueReqNo;
    }

    public void setUniqueReqNo(String uniqueReqNo) {
        this.uniqueReqNo = uniqueReqNo;
    }

    public List<UserMessageRes> getUserMessageRes() {
        return userMessageRes;
    }

    public void setUserMessageRes(List<UserMessageRes> userMessageRes) {
        this.userMessageRes = userMessageRes;
    }

    public List<RiskMessageRes> getRiskMessageRes() {
        return riskMessageRes;
    }

    public void setRiskMessageRes(List<RiskMessageRes> riskMessageRes) {
        this.riskMessageRes = riskMessageRes;
    }
}

class RiskMessageRes{
    private String creditAmt;
}