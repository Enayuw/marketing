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
     * 上报结果 0-上报中，1-上报成功，2-上报失败
     */
    private Integer submitResult;

    /**
     * 最后登录时间
     */
    private String lastlogintime;

    /**
     * 授信申请时间
     */
    private String financeapplytime;

    /**
     * 金融授信状态：0 失败 1 成功
     */
    private String financecreditstatus;

    /**
     * 授信完成时间
     */
    private String financecreditfinishtime;

    /**
     * 提现申请时间
     */
    private String debttime;

    /**
     * 提现通过时间
     */
    private String debtpasstime;

    /**
     * 提现金额
     */
    private String loanamt;

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