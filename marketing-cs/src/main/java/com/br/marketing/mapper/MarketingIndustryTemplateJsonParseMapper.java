package com.br.marketing.mapper;


import com.br.marketing.entity.MarketingIndustryTemplateJsonParse;

import java.util.List;

public interface MarketingIndustryTemplateJsonParseMapper extends MarketingIndustryTemplateJsonParseMapperBase{

    int batchInsert(List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseList);


}
