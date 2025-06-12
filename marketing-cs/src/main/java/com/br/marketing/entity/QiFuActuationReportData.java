package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName QiFuActuationReportData
 * @Author kongbx
 * @Date 2025/6/10 14:45
 */
@Data
public class QiFuActuationReportData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 月份
     */
    private String issueMonth;
    /**
     * 下发日期
     */
    private String issueDate;
    /**
     * 用户类型
     */
    private String userType;
    /**
     * 供应商
     */
    private String supplier;
    /**
     * 有效期
     */
    private String validDate;
    /**
     * 授信人数
     */
    private Integer creditUserCount;
    /**
     * app 登录人数
     */
    private Integer appLoginUserCount;
    /**
     * 发起人数
     */
    private Integer startUserCount;
    /**
     * 动支人数_首动支
     */
    private Integer userLoanCount;
    /**
     * app 登录率
     */
    private BigDecimal appLoginRate;
    /**
     * 人头发起率
     */
    private BigDecimal userStartRate;
    /**
     * 人头动支率_ 首动支
     */
    private BigDecimal userLoanRate;
}
