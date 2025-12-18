package com.br.marketing.mapper;

import com.br.marketing.entity.ReportTask;
import com.br.marketing.entity.ReportTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReportTaskMapperBase {
    int countByExample(ReportTaskExample example);

    int deleteByExample(ReportTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ReportTask record);

    int insertSelective(ReportTask record);

    List<ReportTask> selectByExample(ReportTaskExample example);

    ReportTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ReportTask record, @Param("example") ReportTaskExample example);

    int updateByExample(@Param("record") ReportTask record, @Param("example") ReportTaskExample example);

    int updateByPrimaryKeySelective(ReportTask record);

    int updateByPrimaryKey(ReportTask record);
}