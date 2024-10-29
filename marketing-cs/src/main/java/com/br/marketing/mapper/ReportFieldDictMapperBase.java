package com.br.marketing.mapper;

import com.br.marketing.entity.ReportFieldDict;
import com.br.marketing.entity.ReportFieldDictExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportFieldDictMapperBase {
    int countByExample(ReportFieldDictExample example);

    int deleteByExample(ReportFieldDictExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ReportFieldDict record);

    int insertSelective(ReportFieldDict record);

    List<ReportFieldDict> selectByExample(ReportFieldDictExample example);

    ReportFieldDict selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ReportFieldDict record, @Param("example") ReportFieldDictExample example);

    int updateByExample(@Param("record") ReportFieldDict record, @Param("example") ReportFieldDictExample example);

    int updateByPrimaryKeySelective(ReportFieldDict record);

    int updateByPrimaryKey(ReportFieldDict record);
}