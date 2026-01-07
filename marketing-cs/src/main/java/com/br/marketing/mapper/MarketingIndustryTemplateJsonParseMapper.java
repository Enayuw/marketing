package com.br.marketing.mapper;


import com.br.marketing.entity.MarketingIndustryTemplateJsonParse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingIndustryTemplateJsonParseMapper extends MarketingIndustryTemplateJsonParseMapperBase {

    int batchInsert(List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseList);

    int deleteJsonParseList(@Param("templateId") Long templateId);

}
