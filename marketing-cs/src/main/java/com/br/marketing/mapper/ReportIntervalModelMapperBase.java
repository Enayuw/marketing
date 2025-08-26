package com.br.marketing.mapper;

import com.br.marketing.entity.ReportIntervalModel;
import com.br.marketing.entity.ReportIntervalModelExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportIntervalModelMapperBase {
    int countByExample(ReportIntervalModelExample example);

    int deleteByExample(ReportIntervalModelExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ReportIntervalModel record);

    int insertSelective(ReportIntervalModel record);

    List<ReportIntervalModel> selectByExample(ReportIntervalModelExample example);

    ReportIntervalModel selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ReportIntervalModel record, @Param("example") ReportIntervalModelExample example);

    int updateByExample(@Param("record") ReportIntervalModel record, @Param("example") ReportIntervalModelExample example);

    int updateByPrimaryKeySelective(ReportIntervalModel record);

    int updateByPrimaryKey(ReportIntervalModel record);
}