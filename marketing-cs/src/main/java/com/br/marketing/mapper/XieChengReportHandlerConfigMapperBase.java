package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengReportHandlerConfig;
import com.br.marketing.entity.XieChengReportHandlerConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengReportHandlerConfigMapperBase {
    int countByExample(XieChengReportHandlerConfigExample example);

    int deleteByExample(XieChengReportHandlerConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengReportHandlerConfig record);

    int insertSelective(XieChengReportHandlerConfig record);

    List<XieChengReportHandlerConfig> selectByExample(XieChengReportHandlerConfigExample example);

    XieChengReportHandlerConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengReportHandlerConfig record, @Param("example") XieChengReportHandlerConfigExample example);

    int updateByExample(@Param("record") XieChengReportHandlerConfig record, @Param("example") XieChengReportHandlerConfigExample example);

    int updateByPrimaryKeySelective(XieChengReportHandlerConfig record);

    int updateByPrimaryKey(XieChengReportHandlerConfig record);
}