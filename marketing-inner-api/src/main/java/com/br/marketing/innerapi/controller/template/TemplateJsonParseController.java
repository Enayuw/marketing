package com.br.marketing.innerapi.controller.template;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.service.template.TemplateJsonParseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "行业模板Json数据相关接口", description = "行业模板Json数据相关接口")
public class TemplateJsonParseController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateJsonParseController.class);

    @Resource
    private TemplateJsonParseService templateJsonParseService;

    @Operation(summary = "根据三级部门及数据类型查询行业模板", description = "根据三级部门及数据类型查询行业模板")
    @PostMapping(value = "/queryTemplateJsonParse")
    public ApiResult<JSONArray> queryTemplateJsonParse(@RequestParam(name = "firstDepartment") String firstDepartment,
                                                       @RequestParam(name = "secondDepartment") String secondDepartment,
                                                       @RequestParam(name = "apiType") String apiType,
                                                       @RequestParam(name = "systemType") Integer systemType,
                                                       @RequestParam(name = "dataType") Integer dataType) {
        try {
            Result<JSONArray> result =
                    templateJsonParseService.queryIndustryTemplateJsonParses(firstDepartment, secondDepartment, apiType
                            , systemType, dataType, DataProcessEnum.AcceptTypeEnum.GENERAL.getCode(), true);
            return new ApiResult<JSONArray>().fromResult(result, 1);
        } catch (Exception e) {
            logger.error("根据三级部门及数据类型查询行业模板异常，message:{}", e.getMessage());
            return new ApiResult<JSONArray>().fail().setMessage(e.getMessage()).setData(null);
        }
    }
}
