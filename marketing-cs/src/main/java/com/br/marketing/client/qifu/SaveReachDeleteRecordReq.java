package com.br.marketing.client.qifu;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * 保存触达删除记录
 *
 * @author Guo Zeqiang
 * @dateTime 2023-09-20 17:29
 */
public class SaveReachDeleteRecordReq extends BizData {

    private static final long serialVersionUID = 2990244619111277906L;
    /**
     * 2023-09-20 17:31
     * 运营商
     * 必填
     * 加签
     */
    private String agentOperator;
    /**
     * 2023-09-20 17:31
     * 批次号
     * 必填
     * 加签
     */
    private String batchNo;
    /**
     * 2023-09-20 17:31
     * 流水号 UUID
     * 必填
     * 加签
     */
    private String requestNo;

    public SaveReachDeleteRecordReq() {
        this.requestNo = UUID.randomUUID().toString().replaceAll("-", "") + System.nanoTime();
    }

    @SuppressWarnings("unused")
    public SaveReachDeleteRecordReq(String agentOperator, String batchNo, String requestNo) {
        this.agentOperator = agentOperator;
        this.batchNo = batchNo;
        this.requestNo = StringUtils.isNotBlank(requestNo) ? requestNo
                : UUID.randomUUID().toString().replaceAll("-", "") + System.nanoTime();
    }

    public SaveReachDeleteRecordReq(String agentOperator, String batchNo) {
        this.agentOperator = agentOperator;
        this.batchNo = batchNo;
        this.requestNo = UUID.randomUUID().toString().replaceAll("-", "") + System.nanoTime();
    }

    public String getAgentOperator() {
        return agentOperator;
    }

    public void setAgentOperator(String agentOperator) {
        this.agentOperator = agentOperator;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    @Override
    public String toString() {
        return "SaveReachDeleteRecord{" +
                "agentOperator='" + agentOperator + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", requestNo='" + requestNo + '\'' +
                '}';
    }
}
