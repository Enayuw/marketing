package com.br.marketing.service;

import com.br.marketing.entity.IdempotentRecordInfo;
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
     * 根据幂等键查询幂等记录
     * @param tableType 表类型
     * @param idempotentKey 幂等键
     * @return 幂等记录信息（包含id和apiCode），如果不存在返回null
     */
    IdempotentRecordInfo selectByIdempotentKey(MqIdempotentTableType tableType, Long idempotentKey);

    /**
     * 同时更新isFinished和apiCode（用一个SQL完成）
     * @param tableType 表类型
     * @param recordId 记录ID
     * @param apiCode 客户编号（可为null，如果为null则更新为null）
     */
    void updateIsFinishedAndApiCode(MqIdempotentTableType tableType, Long recordId, String apiCode);

}

