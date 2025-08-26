package com.br.marketing.mapper;

import com.br.marketing.entity.ReportStatisticsScore;
import com.br.marketing.entity.ReportStatisticsScoreExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReportStatisticsScoreBaseMapper {
    int countByExample(ReportStatisticsScoreExample example);

    int deleteByExample(ReportStatisticsScoreExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ReportStatisticsScore record);

    int insertSelective(ReportStatisticsScore record);

    List<ReportStatisticsScore> selectByExample(ReportStatisticsScoreExample example);

    ReportStatisticsScore selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ReportStatisticsScore record, @Param("example") ReportStatisticsScoreExample example);

    int updateByExample(@Param("record") ReportStatisticsScore record, @Param("example") ReportStatisticsScoreExample example);

    int updateByPrimaryKeySelective(ReportStatisticsScore record);

    int updateByPrimaryKey(ReportStatisticsScore record);
}