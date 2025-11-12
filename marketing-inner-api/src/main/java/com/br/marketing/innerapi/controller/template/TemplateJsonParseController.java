package com.br.marketing.innerapi.controller.template;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.service.template.TemplateJsonParseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName TemplateJsonParseController
 * @Author hang.zhou
 * @Date 2025/10/27
 */
@RestController
@RequestMapping("/templateJsonParse")
@Api(value = "行业模板Json数据相关接口", tags = "行业模板Json数据相关接口", produces = "application/json", consumes = "application/json", protocols = "http")
public class TemplateJsonParseController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateJsonParseController.class);

    @Resource
    private TemplateJsonParseService templateJsonParseService;

    @ApiOperation("根据三级部门及数据类型查询行业模板")
    @PostMapping(value = "/queryTemplateJsonParse")
    public ApiResult<JSONArray> queryTemplateJsonParse(@RequestParam(name = "firstDepartment") String firstDepartment,
                                                       @RequestParam(name = "secondDepartment") String secondDepartment,
                                                       @RequestParam(name = "apiType") String apiType,
                                                       @RequestParam(name = "dataType") Integer dataType,
                                                       @RequestParam(name = "systemType") Integer systemType) {
        try {
            Result<JSONArray> result = templateJsonParseService.queryIndustryTemplateJsonParses(firstDepartment, secondDepartment, apiType, systemType, dataType);
            if (result.isSuccess()) {
                return new ApiResult<JSONArray>().success().setData(result.getData());
            } else {
                return new ApiResult<JSONArray>().fail().setMessage(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("根据三级部门及数据类型查询行业模板异常，message:{}", e.getMessage());
            return new ApiResult<JSONArray>().fail().setMessage(e.getMessage()).setData(null);
        }
    }
}
