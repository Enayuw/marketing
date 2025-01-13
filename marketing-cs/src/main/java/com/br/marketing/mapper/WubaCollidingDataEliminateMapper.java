package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataEliminateMapper extends WubaCollidingDataEliminateMapperBase {
    List<String> selectDuplicateData(@Param("list") List<String> list);
    void batchSaveDataByStatusAndPushTime(@Param("list") List<WubaCollidingData> list);
}