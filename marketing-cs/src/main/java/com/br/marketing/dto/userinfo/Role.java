package com.br.marketing.dto.userinfo;

public class Role {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String roleId;
    private Integer category;
    private String apiCode;
    private String name;
    private Integer platform;
    private Integer business;
    private Integer isDelete;
    private Integer roleType;

    public Role() {
    }

    public Integer getId() {
        return this.id;
    }

    public String getRoleId() {
        return this.roleId;
    }

    public Integer getCategory() {
        return this.category;
    }

    public String getApiCode() {
        return this.apiCode;
    }

    public String getName() {
        return this.name;
    }

    public Integer getPlatform() {
        return this.platform;
    }

    public Integer getBusiness() {
        return this.business;
    }

    public Integer getIsDelete() {
        return this.isDelete;
    }

    public Integer getRoleType() {
        return this.roleType;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public void setBusiness(Integer business) {
        this.business = business;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }
}
