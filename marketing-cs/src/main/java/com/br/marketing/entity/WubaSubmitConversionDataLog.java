package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class WubaSubmitConversionDataLog {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 上报表id
     */
    private Long dataId;

    /**
     * md5手机号
     */
    private String cell;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 上报结果， 0-上报中，1-转化成功，2-转化失败
     */
    private Integer submitResult;

    /**
     * 最后登录时间
     */
    private String lastLoginTime;

    /**
     * 授信申请时间
     */
    private String financeApplyTime;

    /**
     * 金融授信状态：0 失败 1 成功
     */
    private String financeCreditStatus;

    /**
     * 授信完成时间
     */
    private String financeCreditFinishTime;

    /**
     * 提现申请时间
     */
    private String debtTime;

    /**
     * 提现通过时间
     */
    private String debtPassTime;

    /**
     * 提现金额
     */
    private String loanAmt;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 删除标识 0-正常，1-删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}