package com.br.marketing.mapper;

import com.br.marketing.entity.QifuStrategyReportData;
import com.br.marketing.entity.QifuStrategyReportDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface QifuStrategyReportDataMapperBase {
    long countByExample(QifuStrategyReportDataExample example);

    int deleteByExample(QifuStrategyReportDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(QifuStrategyReportData record);

    int insertSelective(QifuStrategyReportData record);

    List<QifuStrategyReportData> selectByExample(QifuStrategyReportDataExample example);

    QifuStrategyReportData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") QifuStrategyReportData record, @Param("example") QifuStrategyReportDataExample example);

    int updateByExample(@Param("record") QifuStrategyReportData record, @Param("example") QifuStrategyReportDataExample example);

    int updateByPrimaryKeySelective(QifuStrategyReportData record);

    int updateByPrimaryKey(QifuStrategyReportData record);
}