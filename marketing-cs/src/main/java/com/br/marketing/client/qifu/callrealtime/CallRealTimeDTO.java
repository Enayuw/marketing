package com.br.marketing.client.qifu.callrealtime;

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
    private String rTotalAvailableAmt;


    /**
     * 额度到期日期
     */
    private String rTaLastAdjustmentAmount;

    /**
     * 调整前额度
     */
    private String rTaTemporaryAmountExpireDate;

    /**
     * 券名称列表
     */
    private String rCouponInfo;

    /**
     * 券名称
     */
    private String couponName;


}
