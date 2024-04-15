package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengStatisticsReport;
import com.br.marketing.entity.XieChengStatisticsReportExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengStatisticsReportMapperBase {
    int countByExample(XieChengStatisticsReportExample example);

    int deleteByExample(XieChengStatisticsReportExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengStatisticsReport record);

    int insertSelective(XieChengStatisticsReport record);

    List<XieChengStatisticsReport> selectByExample(XieChengStatisticsReportExample example);

    XieChengStatisticsReport selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengStatisticsReport record, @Param("example") XieChengStatisticsReportExample example);

    int updateByExample(@Param("record") XieChengStatisticsReport record, @Param("example") XieChengStatisticsReportExample example);

    int updateByPrimaryKeySelective(XieChengStatisticsReport record);

    int updateByPrimaryKey(XieChengStatisticsReport record);
}