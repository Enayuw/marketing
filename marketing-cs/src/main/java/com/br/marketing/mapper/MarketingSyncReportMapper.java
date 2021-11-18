package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.entity.MarketingSyncReportExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSyncReportMapper {
    int countByExample(MarketingSyncReportExample example);

    int deleteByExample(MarketingSyncReportExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingSyncReport record);

    int insertSelective(MarketingSyncReport record);

    List<MarketingSyncReport> selectByExample(MarketingSyncReportExample example);

    MarketingSyncReport selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingSyncReport record, @Param("example") MarketingSyncReportExample example);

    int updateByExample(@Param("record") MarketingSyncReport record, @Param("example") MarketingSyncReportExample example);

    int updateByPrimaryKeySelective(MarketingSyncReport record);

    int updateByPrimaryKey(MarketingSyncReport record);
}