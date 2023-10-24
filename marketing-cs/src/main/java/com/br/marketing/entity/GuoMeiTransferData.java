package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;

public class GuoMeiTransferData implements Serializable {
    private static final long serialVersionUID = -5677082629530893177L;
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
    private String requestid;

    /**
     * 渠道编码
     */
    private String channelcode;

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

    public String getRequestid() {
        return requestid;
    }

    public void setRequestid(String requestid) {
        this.requestid = requestid == null ? null : requestid.trim();
    }

    public String getChannelcode() {
        return channelcode;
    }

    public void setChannelcode(String channelcode) {
        this.channelcode = channelcode == null ? null : channelcode.trim();
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
}