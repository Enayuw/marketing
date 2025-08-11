package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

/***
 * @ClassName QiFuEffectDTO
 * @Author hang.zhou
 * @Date 2025/8/6
 */
@Data
public class QiFuEffectReportData {

    /**
     *
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 归属月份 yyyy-MM-dd
     */
    private String belongMonth;

    /**
     * 策略月份 yyyy-MM-dd
     */
    private String strategyMonth;

    /**
     * 更新日期 yyyy-MM-dd
     */
    private String updDate;

    /**
     * 画布名称
     */
    private String canvasName;

    /**
     * 代运营供应商code
     */
    private String agentOperator;

    /**
     * 分组
     */
    private String groupName;

    /**
     * 名单量
     */
    private Integer userCount;

    /**
     * 登录用户数
     */
    private Integer loginUserCount;

    /**
     * 完件用户数
     */
    private Integer applySubmitUserCount;

    /**
     * 授信用户数
     */
    private Integer creditSuccessUserCount;

    /**
     * 登录率
     */
    private String loginRate;

    /**
     * 完件率
     */
    private String applySubmitRate;

    /**
     * 通过率
     */
    private String passRate;

    /**
     * 授信率
     */
    private String creditSuccessRate;

    /**
     * delta完件率
     */
    private String deltaApplySubmitRate;

    /**
     * delta授信率
     */
    private String deltaCreditSuccessRate;

    /**
     * delta完件量
     */
    private Integer deltaApplySubmitCount;

    /**
     * delta授信量
     */
    private Integer deltaCreditSuccessCount;

    /**
     * 归因完件用户数
     */
    private Integer attrApplyUserCount;

    /**
     * 归因授信用户数
     */
    private Integer attrCreditUserCount;

    /**
     * 归因授信用户数A
     */
    private Integer attrCreditUserCountA;

    /**
     * 归因授信用户数B
     */
    private Integer attrCreditUserCountB;

    /**
     * 归因授信用户数C
     */
    private Integer attrCreditUserCountC;

    /**
     * 归因完件占比
     */
    private String attrApplyRatio;

    /**
     * 归因授信占比
     */
    private String attrCreditRatio;

    /**
     * 归因完件率
     */
    private String attrApplyRate;

    /**
     * 归因授信率
     */
    private String attrCreditRate;

    /**
     * 归因授信用户量A占比
     */
    private String attrCreditCountRatioA;

    /**
     * 归因授信用户量B占比
     */
    private String attrCreditCountRatioB;

    /**
     * 归因授信用户量C占比
     */
    private String attrCreditCountRatioC;

    /**
     * 归因人均授信额度
     */
    private String attrAvgCreditLimit;

    /**
     * 归因人头发起率m0
     */
    private String attrUserPerRate;

    /**
     * 归因人头动支率m0
     */
    private String attrUserActRate;

    /**
     * 归因人头发起通过率
     */
    private String attrUserPerAprlRate;

    /**
     * 归因金额发起率m0
     */
    private String attrAmtPerRate;

    /**
     * 归因金额动支率m0
     */
    private String attrAmtActRate;

    /**
     * 归因金额发起通过率m0
     */
    private String attrAmtPerAprlRate;

    /**
     * 归因授信户均动支金额m0
     */
    private String attrCreditAvgActAmt;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;


}
