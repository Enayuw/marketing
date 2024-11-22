package com.br.marketing.mapper;

import com.br.marketing.entity.ReportTaskAction;
import com.br.marketing.entity.ReportTaskActionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReportTaskActionMapper {
    int countByExample(ReportTaskActionExample example);

    int deleteByExample(ReportTaskActionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ReportTaskAction record);

    int insertSelective(ReportTaskAction record);

    List<ReportTaskAction> selectByExample(ReportTaskActionExample example);

    ReportTaskAction selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ReportTaskAction record, @Param("example") ReportTaskActionExample example);

    int updateByExample(@Param("record") ReportTaskAction record, @Param("example") ReportTaskActionExample example);

    int updateByPrimaryKeySelective(ReportTaskAction record);

    int updateByPrimaryKey(ReportTaskAction record);
}