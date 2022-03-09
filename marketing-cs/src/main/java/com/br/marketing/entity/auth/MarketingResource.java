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
    private Integer parentid;

    /**
     * 资源顺序
     */
    private Integer sort;

    /**
     * 
     */
    private Date createdtime;

    /**
     * 
     */
    private Date modifiedtime;

    /**
     * 是否删除 0未被删除1已删除
     */
    private Integer isdelete;

    /**
     * 资源英文名
     */
    private String englishname;

    /**
     * 资源类型（1:菜单，2、页面，3、按钮）
     */
    private Integer category;

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

    public Integer getParentid() {
        return parentid;
    }

    public void setParentid(Integer parentid) {
        this.parentid = parentid;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Date getCreatedtime() {
        return createdtime;
    }

    public void setCreatedtime(Date createdtime) {
        this.createdtime = createdtime;
    }

    public Date getModifiedtime() {
        return modifiedtime;
    }

    public void setModifiedtime(Date modifiedtime) {
        this.modifiedtime = modifiedtime;
    }

    public Integer getIsdelete() {
        return isdelete;
    }

    public void setIsdelete(Integer isdelete) {
        this.isdelete = isdelete;
    }

    public String getEnglishname() {
        return englishname;
    }

    public void setEnglishname(String englishname) {
        this.englishname = englishname == null ? null : englishname.trim();
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }
}