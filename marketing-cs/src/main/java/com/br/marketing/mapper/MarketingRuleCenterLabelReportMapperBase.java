package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingRuleCenterLabelReport;
import com.br.marketing.entity.MarketingRuleCenterLabelReportExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingRuleCenterLabelReportMapperBase {
    int countByExample(MarketingRuleCenterLabelReportExample example);

    int deleteByExample(MarketingRuleCenterLabelReportExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingRuleCenterLabelReport record);

    int insertSelective(MarketingRuleCenterLabelReport record);

    List<MarketingRuleCenterLabelReport> selectByExample(MarketingRuleCenterLabelReportExample example);

    MarketingRuleCenterLabelReport selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingRuleCenterLabelReport record, @Param("example") MarketingRuleCenterLabelReportExample example);

    int updateByExample(@Param("record") MarketingRuleCenterLabelReport record, @Param("example") MarketingRuleCenterLabelReportExample example);

    int updateByPrimaryKeySelective(MarketingRuleCenterLabelReport record);

    int updateByPrimaryKey(MarketingRuleCenterLabelReport record);
}