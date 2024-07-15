package com.br.marketing.mapper;

import com.br.marketing.entity.SushangPushResultData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SushangPushResultDataMapper extends SushangPushResultDataMapperBase {
    void insertBatch(@Param("list") List<SushangPushResultData> resultDataList);

    List<SushangPushResultData> getDealDataByCustNum(@Param("custNums") List<String> custNums, @Param("date") String date);
}
