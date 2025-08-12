package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class MarketingSyncLabel {

    /**
     *
     */
    private Long id;

    /**
     * 标签ID
     */
    private Long labelId;

    /**
     * 上传明细表ID
     */
    private Long syncId;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 新场景-替代group_type
     */
    private String userType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 执行日期
     */
    private String appletDate;

    /**
     * 预留剔除状态字段 1：正常，2：剔除
     */
    private Integer status;


    /**
     * 是否重复 1-未去重; 2-不重复;3-重复;
     */
    private Integer isRepeat;


}
