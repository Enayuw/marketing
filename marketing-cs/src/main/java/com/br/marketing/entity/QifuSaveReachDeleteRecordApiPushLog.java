package com.br.marketing.entity;

import java.util.Date;

public class QifuSaveReachDeleteRecordApiPushLog {
    /**
     *
     */
    private Long id;

    /**
     * 用户编号
     */
    private String apiCode;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 状态：成功:S,失败: F
     */
    private String respFlag;

    /**
     * 返回码
     */
    private String respCode;

    /**
     * code对应的描述
     */
    private String respMsg;

    /**
     * 操作是否成功 成功 Y,失败 N
     */
    private String qifuIsSucceed;

    /**
     * 提示信息,失败场景，原因
     */
    private String qifuMessage;

    /**
     * 上传数据入库日期，格式：yyyy-MM-dd
     */
    private String syncAppletDate;

    /**
     * 状态：已补偿 -1; 补偿 0; 正常 1; 异常 2; 重试后正常 3
     */
    private Integer status;

    /**
     * 推送日期，格式：yyyy-MM-dd
     */
    private String pushDate;

    /**
     * 业务异常信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 流水号
     */
    private String requestNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo == null ? null : batchNo.trim();
    }

    public String getRespFlag() {
        return respFlag;
    }

    public void setRespFlag(String respFlag) {
        this.respFlag = respFlag == null ? null : respFlag.trim();
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode == null ? null : respCode.trim();
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg == null ? null : respMsg.trim();
    }

    public String getQifuIsSucceed() {
        return qifuIsSucceed;
    }

    public void setQifuIsSucceed(String qifuIsSucceed) {
        this.qifuIsSucceed = qifuIsSucceed == null ? null : qifuIsSucceed.trim();
    }

    public String getQifuMessage() {
        return qifuMessage;
    }

    public void setQifuMessage(String qifuMessage) {
        this.qifuMessage = qifuMessage == null ? null : qifuMessage.trim();
    }

    public String getSyncAppletDate() {
        return syncAppletDate;
    }

    public void setSyncAppletDate(String syncAppletDate) {
        this.syncAppletDate = syncAppletDate == null ? null : syncAppletDate.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPushDate() {
        return pushDate;
    }

    public void setPushDate(String pushDate) {
        this.pushDate = pushDate == null ? null : pushDate.trim();
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg == null ? null : errorMsg.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo == null ? null : requestNo.trim();
    }
}