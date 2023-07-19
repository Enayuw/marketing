package com.br.marketing.entity;


public class MarketingNewTransferData {

    /**
     * 手机号同custnum
     */
    String sha256CodeList;

    /**
     * 核验结果 true：参与营销，false：不参与营销
     */
    String result;

    /**
     * 携程用户：CTRIP 去哪儿用户：QUNAR
     */
    String orgChannel;

    /**
     * 营销档位（具体值由运营同学实际定义为准）如：重点营销，次重点营销
     */
    String mktLevel;

    public String getSha256CodeList() {
        return sha256CodeList;
    }

    public void setSha256CodeList(String sha256CodeList) {
        this.sha256CodeList = sha256CodeList;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getOrgChannel() {
        return orgChannel;
    }

    public void setOrgChannel(String orgChannel) {
        this.orgChannel = orgChannel;
    }

    public String getMktLevel() {
        return mktLevel;
    }

    public void setMktLevel(String mktLevel) {
        this.mktLevel = mktLevel;
    }
}