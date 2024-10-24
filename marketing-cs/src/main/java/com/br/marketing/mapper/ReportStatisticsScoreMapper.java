package com.br.marketing.mapper;

import com.br.marketing.dto.dewu.DewuPushQueryQuantityDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ReportStatisticsScoreMapper extends ReportStatisticsScoreBaseMapper {


    List<Map<String, Object>> queryDataMapNumbI_(@Param("querySql") String querySql);


    Integer queryNumBybI_(@Param("querySql") String scoreSql);
}
