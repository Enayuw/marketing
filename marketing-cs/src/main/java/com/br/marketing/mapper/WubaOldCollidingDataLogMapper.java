package com.br.marketing.mapper;

import com.br.marketing.entity.WubaOldCollidingDataLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaOldCollidingDataLogMapper extends WubaOldCollidingDataLogMapperBase {
    void batchSaveByBatchNo(@Param("list") List<WubaOldCollidingDataLog> list);

    void batchUpdateResultById(@Param("logs") List<WubaOldCollidingDataLog> list, @Param("result") Boolean result);

    void updateByBatchNoAndCell(@Param("batchNo") String batchNo, @Param("cell") String cell, @Param("result") Boolean result,
                                @Param("status") String status,
                                @Param("extend") String extend);
}