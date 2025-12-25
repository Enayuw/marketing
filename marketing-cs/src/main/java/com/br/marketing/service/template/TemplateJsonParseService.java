package com.br.marketing.service.template;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingBuildInTemplateJsonParse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TemplateJsonParseService {

    Result<JSONArray> queryIndustryTemplateJsonParses(String firstDepartment, String secondDepartment
            , String apiType, Integer systemType, Integer dataType, Integer acceptType, Boolean needBuildInTemplate);

    List<MarketingBuildInTemplateJsonParse> queryBuildInTemplateJsonParses(Integer systemType, Integer dataType, Integer acceptType);

}
