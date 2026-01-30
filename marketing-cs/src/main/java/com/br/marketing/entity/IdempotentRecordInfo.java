package com.br.marketing.entity;

import lombok.Data;

/**
 * 幂等记录信息
 * 用于查询幂等记录时返回id、apiCode和isFinished
 */
@Data
public class IdempotentRecordInfo {
    /**
     * 记录ID
     */
    private Long id;

    /**
     * 客户编号
     */
    private String apiCode;

    /**
     * 是否业务已执行完成 0-否，1-是
     */
    private Integer isFinished;
}
