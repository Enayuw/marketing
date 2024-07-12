package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataLogMapper extends WubaCollidingDataLogMapperBase {
    void batchSaveByBatchNo(@Param("list") List<WubaCollidingDataLog> list);

    void batchUpdateResultById(@Param("logs") List<WubaCollidingDataLog> list, @Param("result") Boolean result);
}