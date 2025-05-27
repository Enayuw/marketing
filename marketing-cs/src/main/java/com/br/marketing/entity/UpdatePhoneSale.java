package com.br.marketing.entity;

import java.util.Date;

public class UpdatePhoneSale {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private String localId;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer status;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 用户id
     */
    private String uid;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 手机号aes加密
     */
    private String phoneAes;

    /**
     * 姓名
     */
    private String name;

    /**
     * 姓名aes加密
     */
    private String nameAes;

    /**
     * 性别
     */
    private String gender;

    /**
     * 机构名称
     */
    private String orgname;

    /**
     * 数据源
     */
    private String source;

    /**
     * 机构运营场景
     */
    private String userType;

    /**
     * 计划外呼时间
     */
    private String planCallTime;

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

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId == null ? null : localId.trim();
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getPhoneAes() {
        return phoneAes;
    }

    public void setPhoneAes(String phoneAes) {
        this.phoneAes = phoneAes == null ? null : phoneAes.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getNameAes() {
        return nameAes;
    }

    public void setNameAes(String nameAes) {
        this.nameAes = nameAes == null ? null : nameAes.trim();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender == null ? null : gender.trim();
    }

    public String getOrgname() {
        return orgname;
    }

    public void setOrgname(String orgname) {
        this.orgname = orgname == null ? null : orgname.trim();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getPlanCallTime() {
        return planCallTime;
    }

    public void setPlanCallTime(String planCallTime) {
        this.planCallTime = planCallTime == null ? null : planCallTime.trim();
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
}