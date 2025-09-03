package com.br.marketing.mapper;

import com.br.marketing.entity.ScoreStatisticsDetail;
import com.br.marketing.entity.ScoreStatisticsDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ScoreStatisticsDetailBaseMapper {
    int countByExample(ScoreStatisticsDetailExample example);

    int deleteByExample(ScoreStatisticsDetailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ScoreStatisticsDetail record);

    int insertSelective(ScoreStatisticsDetail record);

    List<ScoreStatisticsDetail> selectByExample(ScoreStatisticsDetailExample example);

    ScoreStatisticsDetail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ScoreStatisticsDetail record, @Param("example") ScoreStatisticsDetailExample example);

    int updateByExample(@Param("record") ScoreStatisticsDetail record, @Param("example") ScoreStatisticsDetailExample example);

    int updateByPrimaryKeySelective(ScoreStatisticsDetail record);

    int updateByPrimaryKey(ScoreStatisticsDetail record);
}