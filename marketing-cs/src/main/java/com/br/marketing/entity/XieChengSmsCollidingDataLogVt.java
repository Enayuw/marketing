package com.br.marketing.entity;

import java.util.Date;

public class XieChengSmsCollidingDataLogVt {
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private Long smsCollidingDataVtId;

    /**
     *
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 类型
     */
    private String type;

    /**
     * 手机号
     */
    private String sha256CodeList;

    /**
     * 手机号
     */
    private String md5Code;

    /**
     * 加密前数据
     */
    private String param;

    /**
     * 加密后数据
     */
    private String paramSecret;

    /**
     * 携程用户：CTRIP 去哪儿用户：QUNAR
     */
    private String orgChannel;

    /**
     * 营销档位（具体值由运营同学实际定义为准）如：重点营销，次重点营销
     */
    private String mktLevel;

    /**
     * 手机号当前因保护期等原因导致暂时不能营销，但后续可重新撞库判断是否可营销,返回值：后续可再次撞库
     */
    private String info;

    /**
     * 核验结果 true：参与营销，false：不参与营销
     */
    private Boolean result;

    /**
     * 下次推送时间
     */
    private Date nextPushTime;

    /**
     * 状态 0-正常1-待推送 2 推送完成 3 异常
     */
    private Integer status;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 推送日期

     */
    private Integer sendDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSmsCollidingDataVtId() {
        return smsCollidingDataVtId;
    }

    public void setSmsCollidingDataVtId(Long smsCollidingDataVtId) {
        this.smsCollidingDataVtId = smsCollidingDataVtId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getSha256CodeList() {
        return sha256CodeList;
    }

    public void setSha256CodeList(String sha256CodeList) {
        this.sha256CodeList = sha256CodeList == null ? null : sha256CodeList.trim();
    }

    public String getMd5Code() {
        return md5Code;
    }

    public void setMd5Code(String md5Code) {
        this.md5Code = md5Code == null ? null : md5Code.trim();
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param == null ? null : param.trim();
    }

    public String getParamSecret() {
        return paramSecret;
    }

    public void setParamSecret(String paramSecret) {
        this.paramSecret = paramSecret == null ? null : paramSecret.trim();
    }

    public String getOrgChannel() {
        return orgChannel;
    }

    public void setOrgChannel(String orgChannel) {
        this.orgChannel = orgChannel == null ? null : orgChannel.trim();
    }

    public String getMktLevel() {
        return mktLevel;
    }

    public void setMktLevel(String mktLevel) {
        this.mktLevel = mktLevel == null ? null : mktLevel.trim();
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info == null ? null : info.trim();
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public Date getNextPushTime() {
        return nextPushTime;
    }

    public void setNextPushTime(Date nextPushTime) {
        this.nextPushTime = nextPushTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDataMessage() {
        return dataMessage;
    }

    public void setDataMessage(String dataMessage) {
        this.dataMessage = dataMessage == null ? null : dataMessage.trim();
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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

    public Integer getSendDate() {
        return sendDate;
    }

    public void setSendDate(Integer sendDate) {
        this.sendDate = sendDate;
    }
}