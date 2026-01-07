package com.br.marketing.service.template.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingBuildInTemplateJsonParseMapper;
import com.br.marketing.mapper.MarketingIndustryTemplateJsonParseMapper;
import com.br.marketing.mapper.MarketingIndustryTemplateMapper;
import com.br.marketing.service.template.TemplateJsonParseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class TemplateJsonParseServiceImpl implements TemplateJsonParseService {

    private static final Logger log = LoggerFactory.getLogger(TemplateJsonParseServiceImpl.class);
    @Resource
    private MarketingIndustryTemplateMapper marketingIndustryTemplateMapper;

    @Resource
    private MarketingBuildInTemplateJsonParseMapper marketingBuildInTemplateJsonParseMapper;

    @Resource
    private MarketingIndustryTemplateJsonParseMapper marketingIndustryTemplateJsonParseMapper;

    @Override
    public Result<JSONArray> queryIndustryTemplateJsonParses(String firstDepartment, String secondDepartment
            , String apiType, Integer systemType, Integer dataType, Integer acceptType, Boolean needBuildInTemplate) {
        try {
            //根据apiType和dataType查询行业模板id
            MarketingIndustryTemplateExample templateExample = new MarketingIndustryTemplateExample();
            templateExample.createCriteria().andFirstDepartmentEqualTo(firstDepartment)
                    .andSecondDepartmentEqualTo(secondDepartment)
                    .andApiTypeEqualTo(apiType)
                    .andSystemTypeEqualTo(systemType)
                    .andDataTypeEqualTo(dataType)
                    .andAcceptTypeEqualTo(acceptType)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<MarketingIndustryTemplate> marketingIndustryTemplateList = marketingIndustryTemplateMapper
                    .selectByExample(templateExample);
            Long templateId = 0L;
            if (!marketingIndustryTemplateList.isEmpty()) {
                MarketingIndustryTemplate marketingIndustryTemplate = marketingIndustryTemplateList.get(0);
                templateId = marketingIndustryTemplate.getId();
            }

            //根据模板id查询模板json数据
            MarketingIndustryTemplateJsonParseExample jsonParseExample = new MarketingIndustryTemplateJsonParseExample();
            jsonParseExample.createCriteria().andInterfaceTemplateIdEqualTo(templateId).andIsDelEqualTo(Constants.DATA_VALID);

            List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseList =
                    marketingIndustryTemplateJsonParseMapper.selectByExample(jsonParseExample);
            if (!marketingIndustryTemplateJsonParseList.isEmpty()) {
                return new Result<>().success().setDate(JSON.parseArray(JSON.toJSONString(marketingIndustryTemplateJsonParseList)));
            } else {
                if (needBuildInTemplate) {
                    //若不存在行业模板，返回内置模板
                    List<MarketingBuildInTemplateJsonParse> marketingBuildInTemplateJsonParseList =
                            queryBuildInTemplateJsonParses(systemType, dataType, acceptType);
                    if (!marketingBuildInTemplateJsonParseList.isEmpty()) {
                        return new Result<>().success().setDate(JSON.parseArray(JSON.toJSONString(marketingBuildInTemplateJsonParseList)));
                    } else {
                        log.warn("未查询到模板json数据，查询条件：firstDepartment:{}，secondDepartment:{},apiType:{},systemType:{},dataType:{}",
                                firstDepartment, secondDepartment, apiType, systemType, dataType);
                        return new Result<>().failure().setMessage("模板json数据不存在！");
                    }
                } else {
                    return new Result<>().failure().setMessage("模板json数据不存在！");
                }
                
            }
        } catch (Exception e) {
            log.error("查询模板json数据异常，errorMsg:{}", e.getMessage());
            return new Result<>().failure().setDate(new JSONArray());
        }
    }

    @Override
    public List<MarketingBuildInTemplateJsonParse> queryBuildInTemplateJsonParses(Integer systemType, Integer dataType, Integer acceptType) {
        try {
            MarketingBuildInTemplateJsonParseExample example = new MarketingBuildInTemplateJsonParseExample();
            example.createCriteria().andSystemTypeEqualTo(systemType)
                    .andDataTypeEqualTo(dataType)
                    .andAcceptTypeEqualTo(acceptType);

            List<MarketingBuildInTemplateJsonParse> marketingBuildInTemplateJsonParseList =
                    marketingBuildInTemplateJsonParseMapper.selectByExample(example);
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
