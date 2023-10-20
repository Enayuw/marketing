package com.br.marketing.entity;

import java.util.Date;

public class GuoMeiTransferData {
    /**
     *
     */
    private Long id;

    /**
     * 用户编号
     */
    private String apiCode;

    /**
     * MD5(md5(requestId+channelCode))，32 位大写
     */
    private String sign;

    /**
     * 时间戳+五位以上随机数_批次
     */
    private String requestId;

    /**
     * 渠道编码
     */
    private String channelCode;

    /**
     * 数据状态 0-无效数据、1-同步成功 2-同步转化信息异常、3-同步转化详情异常、4-发送mq失败
     */
    private Integer status;

    /**
     * 接收日期，格式：yyyy-MM-dd
     */
    private String createDate;

    /**
     * 业务异常信息
     */
    private String errorMsg;

    /**
     * 接收的json数据
     */
    private String jsonData;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 数据量
     */
    private Integer dataNumber;

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

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign == null ? null : sign.trim();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode == null ? null : channelCode.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate == null ? null : createDate.trim();
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg == null ? null : errorMsg.trim();
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData == null ? null : jsonData.trim();
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

    public Integer getDataNumber() {
        return dataNumber;
    }

    public void setDataNumber(Integer dataNumber) {
        this.dataNumber = dataNumber;
    }
}