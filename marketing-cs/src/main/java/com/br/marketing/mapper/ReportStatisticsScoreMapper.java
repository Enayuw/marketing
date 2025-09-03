package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ReportStatisticsScoreMapper extends ReportStatisticsScoreBaseMapper {


    List<Map<String, Object>> queryDataMapNumbI_(@Param("querySql") String querySql);

    List<Map<String, Object>> queryDataMapNum(@Param("querySql") String querySql);

    Integer queryNumBybI_(@Param("querySql") String scoreSql);

    void insert(@Param("querySql") String scoreSql);
}
