package com.br.marketing.mapper;

public interface XieChengCollidingDataLoopCycleMapper extends XieChengCollidingDataLoopCycleMapperBase{
    List<XieChengCollidingDataLoopCycle> selectDeleteData(@Param("startTime") String startTime, @Param("size")  int size);
    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size")  int size);
}