package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataBatchNo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface WubaCollidingBatchNoMapper extends WubaCollidingBatchNoMapperBase {
    void saveDataByBatchNo(@Param("batchNo") String batchNo, @Param("batchType") Integer type, @Param("apiCode") String apiCode, @Param(
            "dataSourceType") String dataSourceType);

    List<WubaCollidingDataBatchNo> selectCollidingDataResult(@Param("pushTime") Date pushTime,
                                                             @Param("pageSize") Integer pageSize,
                                                             @Param("apiCode") String apiCode);
}