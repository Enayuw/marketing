package com.br.marketing.dto.userinfo;

public class User {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String userId;
    private Integer userType;
    private String password;
    private String realName;
    private String username;
    private String remark;
    private Integer status;
    private String apiCode;
    private String email;
    private String phone;
    private String companyName;
    private Integer origin;

    public User() {
    }

    public User(User ucUser) {
        this.id = ucUser.getId();
        this.userType = ucUser.getUserType();
        this.password = ucUser.getPassword();
        this.realName = ucUser.getRealName();
        this.phone = ucUser.getPhone();
        this.email = ucUser.getEmail();
        this.username = ucUser.getUsername();
        this.remark = ucUser.getRemark();
        this.status = ucUser.getStatus();
        this.apiCode = ucUser.getApiCode();
        this.companyName = ucUser.getCompanyName();
        this.origin = ucUser.getOrigin();
    }

    public Integer getId() {
        return this.id;
    }

    public String getUserId() {
        return this.userId;
    }

    public Integer getUserType() {
        return this.userType;
    }

    public String getPassword() {
        return this.password;
    }

    public String getRealName() {
        return this.realName;
    }

    public String getUsername() {
        return this.username;
    }

    public String getRemark() {
        return this.remark;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getApiCode() {
        return this.apiCode;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public Integer getOrigin() {
        return this.origin;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setOrigin(Integer origin) {
        this.origin = origin;
    }
}
