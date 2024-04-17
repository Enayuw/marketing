package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class MarketingSyncUserCell {

    /**
     * 上传日期
     */
    private String appletDate;

    /**
     * 客户编号
     */
    private String cid;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 商户名称
     */
    private String shortName;

    /**
     * 新场景-替代group_type
     */
    private String userType;

    /**
     * 手机号对应单条数据总计
     */
    private Long num;

    /**
     * 手机号根据去重规则后的对应单条数据总计
     */
    private Long useRemovalRuleNum;

    /**
     * 本次查询全部结果总数
     */
    private Long total;

    /**
     * 本次查询手机号根据去重规则全部结果总数
     */
    private Long useRemovalRuleTotal;
}