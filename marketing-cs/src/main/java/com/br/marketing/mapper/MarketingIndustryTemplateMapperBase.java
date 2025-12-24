package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingIndustryTemplate;
import com.br.marketing.entity.MarketingIndustryTemplateExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingIndustryTemplateMapperBase {
    int countByExample(MarketingIndustryTemplateExample example);

    int deleteByExample(MarketingIndustryTemplateExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingIndustryTemplate record);

    int insertSelective(MarketingIndustryTemplate record);

    List<MarketingIndustryTemplate> selectByExample(MarketingIndustryTemplateExample example);

    MarketingIndustryTemplate selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingIndustryTemplate record, @Param("example") MarketingIndustryTemplateExample example);

    int updateByExample(@Param("record") MarketingIndustryTemplate record, @Param("example") MarketingIndustryTemplateExample example);

    int updateByPrimaryKeySelective(MarketingIndustryTemplate record);

    int updateByPrimaryKey(MarketingIndustryTemplate record);
}