package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataSyncCleanMapper extends WubaCollidingDataSyncCleanMapperBase {
    void batchSaveData(@Param("list") List<String> list, @Param("batchNo") String batchNo, @Param("apiCode") String apiCode,
                       @Param("taskId") Long taskId);
}