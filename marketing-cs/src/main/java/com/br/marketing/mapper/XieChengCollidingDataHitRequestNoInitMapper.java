package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataHitRequestNoInit;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface XieChengCollidingDataHitRequestNoInitMapper extends XieChengCollidingDataHitRequestNoInitMapperBase{
    List<XieChengCollidingDataHitRequestNoInit> selectData(@Param("minId") Long minId, @Param("pageSize") Integer pageSize);
    /**
     * 更新重试次数：retry_count = retry_count + 1,update_time = now(),push_time = now()
     * @param ids
     * @return
     */
    int updateBatchByIdOfRetryCount(@Param("ids") List<Long> ids);
}