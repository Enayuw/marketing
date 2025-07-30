package com.br.marketing.entity;

import lombok.Data;

/**
 * @ClassName BillReport
 * @Author hang.zhou
 * @Date 2025/7/29
 */
@Data
public class BillReport {

    /**
     * 统计日期
     */
    private String statDate;

    /**
     * 客群名称
     */
    private String exptTemplateName;

    /**
     * 业务流水
     */
    private String bizNo;

    /**
     * 人头发起率排名与第一名差距
     */
    private String userFqRateRnGap;

    /**
     * 单名单放款排名与第一名差距
     */
    private String avgUserLoanAmtRnGap;

    /**
     * 接通率排名与第一名差距
     */
    private String connectRateRnGap;

    /**
     * 单名单外呼次数排名与第一名差距
     */
    private String avgCallCntRnGap;

    /**
     * 接通客户复播次数排名与第一名差距
     */
    private String connectUserCallCntRnGap;

    /**
     * 短信发送率排名与第一名差距
     */
    private String smsRateRnGap;

    /**
     * 语音助手占比与自研差距
     */
    private String connectHRateZyGap;

    /**
     * 静音占比与自研差距
     */
    private String  connectQRateZyGap;

    /**
     * 人头发起率
     */
    private String userFqRate;

    /**
     * 单名单放款
     */
    private String avgUserLoanAmt;

    /**
     * 名单占比
     */
    private String userListRate;

}