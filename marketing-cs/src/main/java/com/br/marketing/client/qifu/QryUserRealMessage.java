package com.br.marketing.client.qifu;

import java.util.List;

public class QryUserRealMessage {

    /**
     *
     */
    private String mobileMd5;
    /**
     *
     */
    private String stopMarketingSign;
    /**
     *
     */
    private List<TradeMessageRes> tradeMessageRes;
    /**
     *
     */
    private String uniqueReqNo;

    private List<RiskMessageRes> riskMessageRes;
    /**
     *
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