package com.br.marketing.client.qifu;

import java.util.List;

/**
 * @ClassName QrySleepUserRealMessageReq
 * @Description TODO
 * @Author kongbx
 * @Date 2024/6/25 16:28
 */
public class QrySleepUserRealMessageReq extends BizData {

    /**
     * 请求信息
     */
    private List<RealDataesReq> realDataes;
    /**
     * 流水号
     */
    private String requestNo;
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 发起类型
     */
    private String initiatingType;
    /**
     * 合作方
     */
    private String partner;

    public List<RealDataesReq> getRealDataes() {
        return realDataes;
    }

    public void setRealDataes(List<RealDataesReq> realDataes) {
        this.realDataes = realDataes;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getInitiatingType() {
        return initiatingType;
    }

    public void setInitiatingType(String initiatingType) {
        this.initiatingType = initiatingType;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    @Override
    public String toString() {
        return "QrySleepUserRealMessageReq{" +
                "realDataes=" + realDataes +
                ", requestNo='" + requestNo + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", initiatingType='" + initiatingType + '\'' +
                ", partner='" + partner + '\'' +
                '}';
    }
}
