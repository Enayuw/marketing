package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface MarketingHaloCallbackRecordMapper extends MarketingHaloCallbackRecordMapperBase {

    void updateStatusByBatchNumber(@Param("status") Integer status, @Param("batchNumber") String batchNumber);
}