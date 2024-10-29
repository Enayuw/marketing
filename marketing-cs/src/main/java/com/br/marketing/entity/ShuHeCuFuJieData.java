package com.br.marketing.entity;

import java.util.Date;

import lombok.Data;

@Data
public class ShuHeCuFuJieData {
    /**
     * 主键id
     */
    private Long id;

    /**
     * sha256加密姓名
     */
    private String nameSha256;

    /**
     * sha256手机号
     */
    private String mobileSha256;

    /**
     * sha256加密身份证号
     */
    private String identificationNoSha256;

    /**
     * 
     */
    private String adtLmt;

    /**
     * 
     */
    private String avlLmt;

    /**
     * 
     */
    private String orderStart;

    /**
     * 
     */
    private String riskPassCnt;

    /**
     * 
     */
    private String fundPassCnt;

    /**
     * 
     */
    private String loanPrincipalAmount;

    /**
     * 
     */
    private String ds;

    /**
     * 
     */
    private String isNonOperate;

    /**
     * 
     */
    private String assetLevel6;

    /**
     * 
     */
    private String lstNonDcpTrsTim;

    /**
     * 
     */
    private String lstOrdTimAllBlbtchhl;

    /**
     * 
     */
    private String lstAdtApyTimHvy;

    /**
     * 
     */
    private String lstAppStaTim;

    /**
     * 
     */
    private String lstMpStaTim;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 
     */
    private Long localId;

    /**
     * apiCode
     */
    private Integer apiCode;

    /**
     * 
     */
    private String dataMessage;

    /**
     * 状态: 0:失效;1:有效;
     */
    private Integer status;
}