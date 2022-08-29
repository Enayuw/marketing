package com.br.marketing.entity.auth;


import java.util.Date;
import java.util.List;
import java.util.Map;

public class MarketingUserInfo {
    /**
     * 
     */
    private Integer id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 描述
     */
    private String remark;

    /**
     * 是否被删除 1未删除 0已删除
     */
    private Integer status;

    /**
     * 0:启用 1：禁用
     */
    private Integer isDisable;

    /**
     * 
     */
    private Integer createUserId;

    /**
     * 
     */
    private Integer updateUserId;

    /**
     * 
     */
    private String email;

    /**
     * 
     */
    private String phone;

    /**
     * 
     */
    private String apiCode;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    private String roleIds;

    private List<Map<String, Object>> roles;

    private Integer passwordEditFlag;

    public MarketingUserInfo(MarketingUserInfo ucUser) {
        this.id = ucUser.getId();
        this.password = ucUser.getPassword();
        this.realName = ucUser.getRealName();
        this.phone = ucUser.getPhone();
        this.email = ucUser.getEmail();
        this.userName = ucUser.getUserName();
        this.remark = ucUser.getRemark();
        this.status = ucUser.getStatus();
        this.apiCode = ucUser.getApiCode();
    }
    public MarketingUserInfo() {

    }

    public List<Map<String, Object>> getRoles() {
        return roles;
    }

    public void setRoles(List<Map<String, Object>> roles) {
        this.roles = roles;
    }

    public String getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(String roleIds) {
        this.roleIds = roleIds;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName == null ? null : realName.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsDisable() {
        return isDisable;
    }

    public void setIsDisable(Integer isDisable) {
        this.isDisable = isDisable;
    }

    public Integer getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Integer createUserId) {
        this.createUserId = createUserId;
    }

    public Integer getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(Integer updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
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

    public Integer getPasswordEditFlag() {
        return passwordEditFlag;
    }

    public void setPasswordEditFlag(Integer passwordEditFlag) {
        this.passwordEditFlag = passwordEditFlag;
    }
}