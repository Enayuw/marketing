package com.br.marketing.mapper;

import com.br.marketing.entity.ReportTaskScoreSource;
import com.br.marketing.entity.ReportTaskScoreSourceExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportTaskScoreSourceMapperBase {
    int countByExample(ReportTaskScoreSourceExample example);

    int deleteByExample(ReportTaskScoreSourceExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ReportTaskScoreSource record);

    int insertSelective(ReportTaskScoreSource record);

    List<ReportTaskScoreSource> selectByExample(ReportTaskScoreSourceExample example);

    ReportTaskScoreSource selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ReportTaskScoreSource record, @Param("example") ReportTaskScoreSourceExample example);

    int updateByExample(@Param("record") ReportTaskScoreSource record, @Param("example") ReportTaskScoreSourceExample example);

    int updateByPrimaryKeySelective(ReportTaskScoreSource record);

    int updateByPrimaryKey(ReportTaskScoreSource record);
}