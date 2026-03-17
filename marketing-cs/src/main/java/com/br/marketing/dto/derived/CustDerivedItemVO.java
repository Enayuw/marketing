package com.br.marketing.dto.derived;

import lombok.Data;

import java.io.Serializable;

/**
 * 客户衍生信息查询单条结果
 */
@Data
public class CustDerivedItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 客户编号 */
    private String custNum;
    /** 原始额度（衍生） */
    private String lowAmount_derived;
    /** 提升额度（衍生） */
    private String changeAmount_derived;
    /** 额度剩余天数（衍生） */
    private String remainDayys_derived;
    /** 提额幅度（衍生） */
    private String changeIncrease_derived;
    /** 定价有效周期 */
    private String pricingValidPeriod;
    /** 定价折扣 */
    private String pricingDiscount;
    /** 定价到期天数 */
    private String pricingExpireDays;
    /** 清洗系统衍生字段1 */
    private String coupon_derived1;
    /** 清洗系统衍生字段2 */
    private String coupon_derived2;
    /** 清洗系统衍生字段3 */
    private String coupon_derived3;
}
