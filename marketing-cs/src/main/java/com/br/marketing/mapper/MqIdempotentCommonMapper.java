package com.br.marketing.mapper;

import com.br.marketing.entity.IdempotentRecordInfo;
import org.apache.ibatis.annotations.Param;


public interface MqIdempotentCommonMapper extends MqIdempotentCommonMapperBase{
    /**
     * 根据幂等键删除记录
     * @param idempotentKey 幂等键
     * @return 删除的记录数
     */
    int deleteByIdempotentKey(@Param("idempotentKey") Long idempotentKey);

    /**
     * 根据幂等键查询记录
     * @param idempotentKey 幂等键
     * @return 幂等记录信息（包含id和apiCode），如果不存在返回null
     */
    IdempotentRecordInfo selectByIdempotentKey(@Param("idempotentKey") Long idempotentKey);
}