package com.br.marketing.service;

import com.br.marketing.enums.MqIdempotentTableType;

/**
 * MQ幂等性服务接口
 */
public interface MqIdempotentService {
    
    /**
     * 插入幂等记录
     * @param tableType 表类型
     * @param idempotentKey 幂等键
     * @param apiCode 客户编号
     * @param tag MQ消息标签
     * @return 幂等记录ID
     */
    Long insertIdempotentRecord(MqIdempotentTableType tableType, Long idempotentKey, String apiCode, String tag);
    
    /**
     * 删除幂等记录（物理删除）
     * @param tableType 表类型
     * @param recordId 记录ID
     */
    void deleteIdempotentRecord(MqIdempotentTableType tableType, Long recordId);
    
    /**
     * 根据幂等键删除幂等记录（物理删除）
     * @param tableType 表类型
     * @param idempotentKey 幂等键
     */
    void deleteIdempotentRecordByKey(MqIdempotentTableType tableType, Long idempotentKey);
    
    /**
     * 更新apiCode
     * @param tableType 表类型
     * @param recordId 记录ID
     * @param apiCode 客户编号
     */
    void updateApiCode(MqIdempotentTableType tableType, Long recordId, String apiCode);
}

