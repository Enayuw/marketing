package com.br.marketing.client.qifu.callrealtime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CallRealTimeDTO {


    /**
     * 唯一识别码
     */
    private String serialNo;


    /**
     * 签名名称
     */
    private String signName;

    /**
     * 支持外呼
     */
    private String supportCall;


    /**
     * 是否提额客户
     */
    private String increaseCustomer;

    /**
     * 是否临时提额
     */
    private String temporaryIncrease;

    /**
     * 最新可用额度
     */
    @JsonProperty("rTotalAvailableAmt")
    private String rTotalAvailableAmt;


    /**
     * 额度到期日期
     */
    @JsonProperty("rTaLastAdjustmentAmount")
    private String rTaLastAdjustmentAmount;

    /**
     * 调整前额度
     */
    @JsonProperty("rTaTemporaryAmountExpireDate")
    private String rTaTemporaryAmountExpireDate;

    /**
     * 券名称列表
     */
    @JsonProperty("rCouponInfo")
    private String rCouponInfo;

    /**
     * 姓氏
     */
    @JsonProperty("surname")
    private String surname;

    /**
     * 性别
     */
    @JsonProperty("gender")
    private String gender;

    /**
     * 定价有效期
     */
    @JsonProperty("pricingValidPeriod")
    private String pricingValidPeriod;

    /**
     * 定价折扣
     */
    @JsonProperty("pricingDiscount")
    private String pricingDiscount;

    /**
     * 定价到期天数
     */
    @JsonProperty("pricingExpireDays")
    private String pricingExpireDays;

    /**
     * 券名称
     */
    private String couponName;

    /**
     * 产品类型
     */
    private String productType;


}
