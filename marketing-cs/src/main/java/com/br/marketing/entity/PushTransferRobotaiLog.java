package com.br.marketing.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class PushTransferRobotaiLog {
    /**
     *
     */
    private Long id;

    /**
     * 客户转化数据请求主键
     */
    private Long transferInfoId;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 请求批次号
     */
    private String requestId;

    /**
     * 响应体
     */
    private String responseBody;

    /**
     * 返回的业务状态码
     */
    private String serviceCode;

    /**
     * 返回的业务提示消息
     */
    private String message;

    /**
     * 发送的数据量
     */
    private Integer rowSize;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 补偿次数
     */
    private Integer compensateTimes;

    /**
     * 推送状态：0 补偿中；1 已补偿；2 超出补偿次数；3 放弃补偿
     */
    private Integer pushStatus;

    /**
     * 请求体json
     */
    private String requestBody;

    /**
     * 分表cid
     */
    private String tCid;

    public PushTransferRobotaiLog() {
    }

    public PushTransferRobotaiLog(Long transferInfoId, String apiCode, String requestId, String responseBody
            , String serviceCode, String message, Integer rowSize, String requestBody, String tCid) {
        this.transferInfoId = transferInfoId;
        this.apiCode = apiCode;
        this.requestId = requestId;
        this.responseBody = responseBody;
        this.serviceCode = serviceCode;
        this.message = message;
        this.rowSize = rowSize;
        this.createTime = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        this.compensateTimes = 0;
        this.pushStatus = 0;
        this.requestBody = requestBody;
        this.tCid = tCid;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransferInfoId() {
        return transferInfoId;
    }

    public void setTransferInfoId(Long transferInfoId) {
        this.transferInfoId = transferInfoId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody == null ? null : responseBody.trim();
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode == null ? null : serviceCode.trim();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? null : message.trim();
    }

    public Integer getRowSize() {
        return rowSize;
    }

    public void setRowSize(Integer rowSize) {
        this.rowSize = rowSize;
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

    public Integer getCompensateTimes() {
        return compensateTimes;
    }

    public void setCompensateTimes(Integer compensateTimes) {
        this.compensateTimes = compensateTimes;
    }

    public Integer getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody == null ? null : requestBody.trim();
    }

    public String gettCid() {
        return tCid;
    }

    public void settCid(String tCid) {
        this.tCid = tCid == null ? null : tCid.trim();
    }
}