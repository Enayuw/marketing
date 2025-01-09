package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataBatchNo;
import com.br.marketing.entity.WubaOldCollidingDataBatchNo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface WubaOldCollidingBatchNoMapper extends WubaOldCollidingDataBatchNoMapperBase {
    void saveDataByBatchNo(@Param("batchNo") String batchNo, @Param("batchType") Integer type, @Param("apiCode") String apiCode, @Param(
            "dataSourceType") String dataSourceType);

    List<WubaOldCollidingDataBatchNo> selectCollidingDataResult(@Param("pageSize") Integer pageSize,
                                                                @Param("apiCode") String apiCode);
}