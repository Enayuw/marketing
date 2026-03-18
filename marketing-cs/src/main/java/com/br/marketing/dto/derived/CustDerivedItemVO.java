package com.br.marketing.dto.derived;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 客户衍生信息查询单条结果（固定字段 + 清洗系统配置的目标字段，动态字段通过 extraFields 平铺到 JSON）
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

    /** 清洗系统配置的 mappingField 动态字段（与固定字段一起平铺到返回 JSON） */
    private Map<String, String> extraFields;

    @JsonAnyGetter
    public Map<String, String> getExtraFields() {
        return extraFields != null ? extraFields : Collections.emptyMap();
    }
}
