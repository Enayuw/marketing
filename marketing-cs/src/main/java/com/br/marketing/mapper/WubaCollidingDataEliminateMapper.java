package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataEliminateMapper extends WubaCollidingDataEliminateMapperBase {
    List<String> selectDuplicateData(@Param("list") List<String> list);

    void batchSaveDataByBatchNoAndPushTime(@Param("list") List<String> list, @Param("apiCode") String apiCode, @Param("batchId") Long batchId);
}