package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_qifu_strategy_report_data
 * @author :zhen.Li1
 * @desc : 360策略效果数据报表
 */
@Data
public class QifuStrategyReportData implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 策略月份
     */
    private String strategyMonth;

    /**
     * 更新日期
     */
    private String updateDate;

    /**
     * 画布名称
     */
    private String canvasName;

    /**
     * 代运营供应商
     */
    private String supplier;

    /**
     * 分组
     */
    private String groupName;

    /**
     * 名单量
     */
    private String userCount;

    /**
     * 完件用户数
     */
    private String applySubmitUserCount;

    /**
     * 授信用户数
     */
    private String creditSuccessUserCount;

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
     * delta 完件率
     */
    private String deltaApplySubmitRate;

    /**
     * delta 授信率
     */
    private String deltaCreditSuccessRate;

    /**
     * delta 完件量
     */
    private String deltaApplySubmitCount;

    /**
     * delta 授信量
     */
    private String deltaCreditSuccessCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}