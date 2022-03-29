package com.br.marketing.entity.auth;

import lombok.Data;

import java.util.Date;

@Data
public class MarketingRole {
    /**
     * 
     */
    private Integer id;

    /**
     * 角色名字(英文）
     */
    private String name;

    /**
     * 角色描述
     */
    private String nameRemark;

    /**
     * 0:正常 1：删除
     */
    private Integer status;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 该角色是否勾选
     */
    private boolean select = false;

    /***
     * 角色权限资源  多个资源逗号分隔
     */
    private String allResource;



}