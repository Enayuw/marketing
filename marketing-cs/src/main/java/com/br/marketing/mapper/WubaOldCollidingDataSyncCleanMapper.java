package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaOldCollidingDataSyncCleanMapper extends WubaOldCollidingDataSyncCleanMapperBase {
    void batchSaveData(@Param("list") List<WubaCollidingData> list, @Param("batchNo") String batchNo, @Param("apiCode") String apiCode,
                       @Param("taskId") Long taskId);
}