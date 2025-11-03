package com.br.marketing.service.template;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingBuildInTemplateJsonParse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IndustryTemplateJsonParseService {

    Result<JSONArray> queryIndustryTemplateJsonParses(String firstDepartment, String secondDepartment, String apiType, Integer dataType);

    List<MarketingBuildInTemplateJsonParse> queryBuildInTemplateJsonParses(Integer dataType);

}
