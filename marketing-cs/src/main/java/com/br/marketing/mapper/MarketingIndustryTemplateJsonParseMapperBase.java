package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingIndustryTemplateJsonParse;
import com.br.marketing.entity.MarketingIndustryTemplateJsonParseExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingIndustryTemplateJsonParseMapperBase {
    int countByExample(MarketingIndustryTemplateJsonParseExample example);

    int deleteByExample(MarketingIndustryTemplateJsonParseExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingIndustryTemplateJsonParse record);

    int insertSelective(MarketingIndustryTemplateJsonParse record);

    List<MarketingIndustryTemplateJsonParse> selectByExample(MarketingIndustryTemplateJsonParseExample example);

    MarketingIndustryTemplateJsonParse selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingIndustryTemplateJsonParse record
            , @Param("example") MarketingIndustryTemplateJsonParseExample example);

    int updateByExample(@Param("record") MarketingIndustryTemplateJsonParse record
            , @Param("example") MarketingIndustryTemplateJsonParseExample example);

    int updateByPrimaryKeySelective(MarketingIndustryTemplateJsonParse record);

    int updateByPrimaryKey(MarketingIndustryTemplateJsonParse record);
}