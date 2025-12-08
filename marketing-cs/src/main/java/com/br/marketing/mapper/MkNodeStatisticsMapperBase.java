package com.br.marketing.mapper;

import com.br.marketing.entity.MkNodeStatistics;
import com.br.marketing.entity.MkNodeStatisticsExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MkNodeStatisticsMapperBase {
    int countByExample(MkNodeStatisticsExample example);

    int deleteByExample(MkNodeStatisticsExample example);

    int insert(MkNodeStatistics record);

    int insertSelective(MkNodeStatistics record);

    List<MkNodeStatistics> selectByExample(MkNodeStatisticsExample example);

    int updateByExampleSelective(@Param("record") MkNodeStatistics record, @Param("example") MkNodeStatisticsExample example);

    int updateByExample(@Param("record") MkNodeStatistics record, @Param("example") MkNodeStatisticsExample example);
}