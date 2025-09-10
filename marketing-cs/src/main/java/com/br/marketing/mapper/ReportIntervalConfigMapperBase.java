package com.br.marketing.mapper;

import com.br.marketing.entity.ReportIntervalConfig;
import com.br.marketing.entity.ReportIntervalConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportIntervalConfigMapperBase {
    int countByExample(ReportIntervalConfigExample example);

    int deleteByExample(ReportIntervalConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ReportIntervalConfig record);

    int insertSelective(ReportIntervalConfig record);

    List<ReportIntervalConfig> selectByExample(ReportIntervalConfigExample example);

    ReportIntervalConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ReportIntervalConfig record, @Param("example") ReportIntervalConfigExample example);

    int updateByExample(@Param("record") ReportIntervalConfig record, @Param("example") ReportIntervalConfigExample example);

    int updateByPrimaryKeySelective(ReportIntervalConfig record);

    int updateByPrimaryKey(ReportIntervalConfig record);
}