package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class CustomerRule {
    /**
     * 
     */
    private Long id;

    /**
     * 客户id
     */
    private Long customerId;

    /**
     * 规则id
     */
    private Long ruleId;

    /**
     * 规则id
     */
    private String conditionInfo;

    /**
     * 删除标志；1-正常；9-删除；
     */
    private Integer isDel;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

}