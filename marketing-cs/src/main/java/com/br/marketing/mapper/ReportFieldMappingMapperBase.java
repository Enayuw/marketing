package com.br.marketing.mapper;

import com.br.marketing.entity.ReportFieldMapping;
import com.br.marketing.entity.ReportFieldMappingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportFieldMappingMapperBase {
    int countByExample(ReportFieldMappingExample example);

    int deleteByExample(ReportFieldMappingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ReportFieldMapping record);

    int insertSelective(ReportFieldMapping record);

    List<ReportFieldMapping> selectByExample(ReportFieldMappingExample example);

    ReportFieldMapping selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ReportFieldMapping record, @Param("example") ReportFieldMappingExample example);

    int updateByExample(@Param("record") ReportFieldMapping record, @Param("example") ReportFieldMappingExample example);

    int updateByPrimaryKeySelective(ReportFieldMapping record);

    int updateByPrimaryKey(ReportFieldMapping record);
}