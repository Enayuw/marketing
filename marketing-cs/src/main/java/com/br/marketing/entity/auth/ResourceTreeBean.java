package com.br.marketing.entity.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 权限资源树bean
 * Created by lmc on 2017/7/7.
 */

public class ResourceTreeBean implements Comparable<ResourceTreeBean> {
    /**
     * 资源id
     */
    private Integer id;
    /**
     * 资源名称
     */
    private String text;

    /**
     * 是否启用图标
     */
    private String icon;

    private Map<String, Object> aAttr;

    private boolean select = false;

    private Map<String, Object> data;
    /***
     * 排序
     */
    private Integer sort;

    /***
     * 子节点
     */
    private List<ResourceTreeBean> children = new ArrayList<>();

    /***
     * 排序
     * @param o
     * @return
     */
    @Override
    public int compareTo(ResourceTreeBean o) {
        int n = o.getSort() - this.sort;
        return n;
    }

    public ResourceTreeBean() {
    }

    public ResourceTreeBean(Integer id, String text, String icon, Integer sort,
                            Map<String, Object> attr, Map<String, Object> data) {
        this.id = id;
        this.text = text;
        this.icon = icon;
        this.sort = sort;
        this.aAttr = attr;
        this.data = data;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Map<String, Object> getaAttr() {
        return aAttr;
    }

    public void setaAttr(Map<String, Object> aAttr) {
        this.aAttr = aAttr;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public List<ResourceTreeBean> getChildren() {
        return children;
    }

    public void setChildren(List<ResourceTreeBean> children) {
        this.children = children;
    }
}
