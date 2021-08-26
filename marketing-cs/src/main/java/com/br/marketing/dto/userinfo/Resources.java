package com.br.marketing.dto.userinfo;

public class Resources {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String icon;
    private String name;
    private String authority;
    private String url;
    private Integer type;
    private String parentId;
    private Integer sort;
    private Integer status;
    private Integer isDelete;
    private String englishName;
    private Integer category;
    private String resourcesId;
    private Integer platform;

    public Resources() {
    }

    public Integer getId() {
        return this.id;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getName() {
        return this.name;
    }

    public String getAuthority() {
        return this.authority;
    }

    public String getUrl() {
        return this.url;
    }

    public Integer getType() {
        return this.type;
    }

    public String getParentId() {
        return this.parentId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public Integer getStatus() {
        return this.status;
    }

    public Integer getIsDelete() {
        return this.isDelete;
    }

    public String getEnglishName() {
        return this.englishName;
    }

    public Integer getCategory() {
        return this.category;
    }

    public String getResourcesId() {
        return this.resourcesId;
    }

    public Integer getPlatform() {
        return this.platform;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public void setResourcesId(String resourcesId) {
        this.resourcesId = resourcesId;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

}
