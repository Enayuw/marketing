package com.br.marketing.mapper;

import com.br.marketing.dto.dewu.DewuPushQueryQuantityDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ReportStatisticsScoreMapper extends ReportStatisticsScoreBaseMapper {


    List<Map<String, String>> queryDataMapNumdoris_(@Param("querySql") String querySql);


}
