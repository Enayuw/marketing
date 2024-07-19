package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataLoopCycleMapper extends WubaCollidingDataLoopCycleMapperBase{
    void batchSaveData(@Param("list") List<String> list, @Param("apiCode") String apiCode);
}