package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingBuildInTemplateJsonParse;
import com.br.marketing.entity.MarketingBuildInTemplateJsonParseExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingBuildInTemplateJsonParseMapperBase {
    int countByExample(MarketingBuildInTemplateJsonParseExample example);

    int deleteByExample(MarketingBuildInTemplateJsonParseExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingBuildInTemplateJsonParse record);

    int insertSelective(MarketingBuildInTemplateJsonParse record);

    List<MarketingBuildInTemplateJsonParse> selectByExample(MarketingBuildInTemplateJsonParseExample example);

    MarketingBuildInTemplateJsonParse selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingBuildInTemplateJsonParse record
            , @Param("example") MarketingBuildInTemplateJsonParseExample example);

    int updateByExample(@Param("record") MarketingBuildInTemplateJsonParse record
            , @Param("example") MarketingBuildInTemplateJsonParseExample example);

    int updateByPrimaryKeySelective(MarketingBuildInTemplateJsonParse record);

    int updateByPrimaryKey(MarketingBuildInTemplateJsonParse record);
}