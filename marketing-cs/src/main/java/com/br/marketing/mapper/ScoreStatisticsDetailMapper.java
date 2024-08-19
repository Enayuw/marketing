package com.br.marketing.mapper;

import com.br.marketing.entity.ScoreStatisticsDetail;
import com.br.marketing.entity.SushangPushResultData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScoreStatisticsDetailMapper extends ScoreStatisticsDetailBaseMapper{

    void insertBatch(@Param("list") List<ScoreStatisticsDetail> statisticsDetails);


}
