package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface WubaCollidingBatchNoMapper extends WubaCollidingBatchNoMapperBase{
    void saveDataByBatchNo(@Param("batchNo") String batchNo,@Param("batchType") Integer type);
}