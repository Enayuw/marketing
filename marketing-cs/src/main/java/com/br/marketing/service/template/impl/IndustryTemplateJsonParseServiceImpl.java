package com.br.marketing.service.template.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingBuildInTemplateJsonParseMapper;
import com.br.marketing.mapper.MarketingIndustryTemplateJsonParseMapper;
import com.br.marketing.mapper.MarketingIndustryTemplateMapper;
import com.br.marketing.service.template.IndustryTemplateJsonParseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName MarketingIndustryTemplateJsonParseServiceImpl
 * @Author hang.zhou
 * @Date 2025/10/31
 */
@Service
public class IndustryTemplateJsonParseServiceImpl implements IndustryTemplateJsonParseService {

    @Resource
    private MarketingIndustryTemplateMapper marketingIndustryTemplateMapper;

    @Resource
    private MarketingBuildInTemplateJsonParseMapper marketingBuildInTemplateJsonParseMapper;

    @Resource
    private MarketingIndustryTemplateJsonParseMapper marketingIndustryTemplateJsonParseMapper;

    @Override
    public Result<JSONArray> queryIndustryTemplateJsonParses(String firstDepartment, String secondDepartment, String apiType, Integer dataType) {
        try {
            //根据apiType和dataType查询行业模板id
            MarketingIndustryTemplateExample templateExample = new MarketingIndustryTemplateExample();
            templateExample.createCriteria().andFirstDepartmentEqualTo(firstDepartment)
                    .andSecondDepartmentEqualTo(secondDepartment)
                    .andApiTypeEqualTo(apiType)
                    .andDataTypeEqualTo(dataType);
            List<MarketingIndustryTemplate> marketingIndustryTemplateList = marketingIndustryTemplateMapper
                    .selectByExample(templateExample);
            Long templateId = 0L;
            if (!marketingIndustryTemplateList.isEmpty()) {
                MarketingIndustryTemplate marketingIndustryTemplate = marketingIndustryTemplateList.get(0);
                templateId = marketingIndustryTemplate.getId();
            }

            //根据模板id查询模板json数据
            MarketingIndustryTemplateJsonParseExample jsonParseExample = new MarketingIndustryTemplateJsonParseExample();
            jsonParseExample.createCriteria().andInterfaceTemplateIdEqualTo(templateId);

            List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseList = marketingIndustryTemplateJsonParseMapper.selectByExample(jsonParseExample);
            if (!marketingIndustryTemplateJsonParseList.isEmpty()) {
                return new Result<>().success().setDate(JSON.parseArray(JSON.toJSONString(marketingIndustryTemplateJsonParseList)));
            } else {
                //若不存在行业模板，返回内置模板
                List<MarketingBuildInTemplateJsonParse> marketingBuildInTemplateJsonParseList = queryBuildInTemplateJsonParses(dataType);
                if (!marketingBuildInTemplateJsonParseList.isEmpty()) {
                    return new Result<>().success().setDate(marketingBuildInTemplateJsonParseList);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Result<>().failure().setDate(new JSONArray());
    }

    @Override
    public List<MarketingBuildInTemplateJsonParse> queryBuildInTemplateJsonParses(Integer dataType) {
        try {
            MarketingBuildInTemplateJsonParseExample example = new MarketingBuildInTemplateJsonParseExample();
            example.createCriteria().andDataTypeEqualTo(dataType);

            List<MarketingBuildInTemplateJsonParse> marketingBuildInTemplateJsonParseList = marketingBuildInTemplateJsonParseMapper.selectByExample(example);
            if (!marketingBuildInTemplateJsonParseList.isEmpty()) {
                return marketingBuildInTemplateJsonParseList;
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
