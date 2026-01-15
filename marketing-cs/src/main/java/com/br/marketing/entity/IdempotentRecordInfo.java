package com.br.marketing.entity;

import lombok.Data;

/**
 * 幂等记录信息
 * 用于查询幂等记录时返回id和apiCode
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
}
