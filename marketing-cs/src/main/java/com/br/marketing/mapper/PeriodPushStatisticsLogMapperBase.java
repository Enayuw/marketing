package com.br.marketing.mapper;

import com.br.marketing.entity.PeriodPushStatisticsLog;
import com.br.marketing.entity.PeriodPushStatisticsLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PeriodPushStatisticsLogMapperBase {
    int countByExample(PeriodPushStatisticsLogExample example);

    int deleteByExample(PeriodPushStatisticsLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PeriodPushStatisticsLog record);

    int insertSelective(PeriodPushStatisticsLog record);

    List<PeriodPushStatisticsLog> selectByExample(PeriodPushStatisticsLogExample example);

    PeriodPushStatisticsLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PeriodPushStatisticsLog record, @Param("example") PeriodPushStatisticsLogExample example);

    int updateByExample(@Param("record") PeriodPushStatisticsLog record, @Param("example") PeriodPushStatisticsLogExample example);

    int updateByPrimaryKeySelective(PeriodPushStatisticsLog record);

    int updateByPrimaryKey(PeriodPushStatisticsLog record);
}