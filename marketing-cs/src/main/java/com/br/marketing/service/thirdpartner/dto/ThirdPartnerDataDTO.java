package com.br.marketing.service.thirdpartner.dto;

import lombok.Data;

/**
 * @Description ThirdPartnerDataDTO
 * @Author hong.chen
 * @CreateTime 2024/11/29
 */
@Data
public class ThirdPartnerDataDTO {
    /**
     * 客户编号
     */
    private String apiCode;

    /**
     * 案件编号
     */
    private String custNum;

    /**
     * 手机号log加密
     */
    private String cell;

    /**
     * 场景
     */
    private String userType;

    /**
     * 子场景
     */
    private String customNameType;

    /**
     * 三方类型 1-百应、2-百可录
     */
    private Integer resourceChannel;

    /**
     * 生效开始日期
     */
    private String validStartDate;

    /**
     * 生效结束日期
     */
    private String validEndDate;
}
