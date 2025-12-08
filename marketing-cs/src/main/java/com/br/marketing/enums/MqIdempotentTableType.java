package com.br.marketing.enums;

/**
 * MQ幂等表类型枚举
 */
public enum MqIdempotentTableType {
    /**
     * 通用表 - b_mq_idempotent_common
     */
    COMMON("common", "b_mq_idempotent_common"),
    
    /**
     * 特殊表 - b_mq_idempotent_special（处理规则中心和mrp服务消息）
     */
    SPECIAL("special", "b_mq_idempotent_special");
    
    private final String code;
    private final String tableName;
    
    MqIdempotentTableType(String code, String tableName) {
        this.code = code;
        this.tableName = tableName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getTableName() {
        return tableName;
    }
}

