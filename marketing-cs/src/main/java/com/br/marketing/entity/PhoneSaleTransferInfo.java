package com.br.marketing.entity;

import java.util.Date;

public class PhoneSaleTransferInfo {
    /**
     *
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 案件编号
     */
    private String cusaNum;

    /**
     * 场景
     */
    private String userType;

    /**
     * 数据上传日期
     */
    private String appletDate;

    /**
     * 数据类型(其他业务功能依次增加编号，并用枚举维护) 1:转化；2:失效数据过滤
     */
    private Integer dataType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 转化规则类型:1：通用规则1（orgname+uid）;2：通用规则2（uid+userType+source+type+phone+orgName）3: 通用规则3(userType+source+type+phone+orgName）
     */
    private String transformStatus;

    /**
     * 源数据id
     */
    private Long sourceId;

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

    public String getCusaNum() {
        return cusaNum;
    }

    public void setCusaNum(String cusaNum) {
        this.cusaNum = cusaNum == null ? null : cusaNum.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate == null ? null : appletDate.trim();
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
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

    public String getTransformStatus() {
        return transformStatus;
    }

    public void setTransformStatus(String transformStatus) {
        this.transformStatus = transformStatus == null ? null : transformStatus.trim();
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }
}