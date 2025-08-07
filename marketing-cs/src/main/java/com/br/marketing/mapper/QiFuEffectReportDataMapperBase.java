package com.br.marketing.mapper;

import com.br.marketing.entity.QiFuEffectReportData;
import com.br.marketing.entity.QiFuEffectReportDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QiFuEffectReportDataMapperBase {
    int countByExample(QiFuEffectReportDataExample example);

    int deleteByExample(QiFuEffectReportDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(QiFuEffectReportData record);

    int insertSelective(QiFuEffectReportData record);

    List<QiFuEffectReportData> selectByExample(QiFuEffectReportDataExample example);

    QiFuEffectReportData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") QiFuEffectReportData record, @Param("example") QiFuEffectReportDataExample example);

    int updateByExample(@Param("record") QiFuEffectReportData record, @Param("example") QiFuEffectReportDataExample example);

    int updateByPrimaryKeySelective(QiFuEffectReportData record);

    int updateByPrimaryKey(QiFuEffectReportData record);
}