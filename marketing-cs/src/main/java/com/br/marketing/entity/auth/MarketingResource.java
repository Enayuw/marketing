package com.br.marketing.entity.auth;

import java.util.Date;

public class MarketingResource {
    /**
     * 
     */
    private Integer id;

    /**
     * 资源图标
     */
    private String icon;

    /**
     * 资源名字
     */
    private String name;

    /**
     * 权限
     */
    private String authority;

    /**
     * 资源路径
     */
    private String url;

    /**
     * 资源类型（1:一级菜单，2:二级菜单，3:三级菜单，4:按钮）
     */
    private Integer type;

    /**
     * 父资源ID
     */
    private Integer parentId;

    /**
     * 资源顺序
     */
    private Integer sort;

    /**
     * 是否删除 1未被删除 0已删除
     */
    private Integer status;

    /**
     * 资源英文名
     */
    private String englishName;

    /**
     * 资源类型（1:菜单，2、页面，3、按钮）
     */
    private Integer category;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon == null ? null : icon.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority == null ? null : authority.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName == null ? null : englishName.trim();
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
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