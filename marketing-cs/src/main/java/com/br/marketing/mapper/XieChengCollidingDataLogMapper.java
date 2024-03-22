package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.XieChengCollidingDataLog;

public interface XieChengCollidingDataLogMapper extends XieChengCollidingDataLogMapperBase {
    List<XieChengCollidingDataLog> selectDeleteData(@Param("startTime") String startTime, @Param("size") int size);

    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size") int size);

    void batchSave(@Param("collidingLogs") List<XieChengCollidingDataLog> collidingLogs);
}