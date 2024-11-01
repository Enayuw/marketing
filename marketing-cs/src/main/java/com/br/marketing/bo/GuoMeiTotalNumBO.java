package com.br.marketing.bo;

import lombok.Data;

/**
 * 总人数（单个”用户类型”+”批次ID”+”计划ID”需要推送的总人数）
 *
 * @author Hua Qiang
 * @date 2024-10-29 21:34
 */
@Data
public class GuoMeiTotalNumBO {

    /**
     * 批次
     */
    private Integer batch;

    /**
     * 计划id
     */
    private Long planId;

    /**
     * 用户类型： 1-可营销，2-不营销
     */
    private Integer userType;

    /**
     * 总人数
     */
    private Long totalNum;
}
