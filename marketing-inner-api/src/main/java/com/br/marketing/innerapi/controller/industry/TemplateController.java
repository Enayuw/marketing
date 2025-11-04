package com.br.marketing.innerapi.controller.industry;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.template.MarketingIndustryTemplateDTO;
import com.br.marketing.entity.MarketingIndustryTemplate;
import com.br.marketing.service.template.IndustryTemplateJsonParseService;
import com.br.marketing.service.template.TemplateService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @ClassName TemplateController
 * @Author hang.zhou
 * @Date 2025/10/27
 */
@RestController
@RequestMapping("/template")
public class TemplateController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @Resource
    private TemplateService templateService;

    @Resource
    private IndustryTemplateJsonParseService industryTemplateJsonParseService;

    /**
     * 新增行业模板
     *
     * @param marketingIndustryTemplateDTO 模板信息
     * @return 是否新增成功
     */
    @ApiOperation("新增模板接口")
    @PostMapping(value = "/addTemplate")
    public ApiResult<Boolean> addTemplate(@RequestBody MarketingIndustryTemplateDTO marketingIndustryTemplateDTO) {
        try {
            Result<Boolean> result = templateService.addTemplate(marketingIndustryTemplateDTO);
            if (result.isSuccess()) {
                return new ApiResult<Boolean>().success().setData(result.getData());
            } else {
                return new ApiResult<Boolean>().fail().setMessage(result.getMessage()).setData(result.getData());
            }
        } catch (Exception e) {
            logger.error("新增行业模板异常,message:{}", e.getMessage());
            return new ApiResult<Boolean>().fail().setMessage(e.getMessage()).setData(Boolean.FALSE);
        }

    }

    /**
     * 查询行业模板接口
     *
     * @param current          当前页
     * @param pageSize         页大小
     * @param templateName     行业模板名称
     * @param firstDepartment  一级部门
     * @param secondDepartment 二级部门
     * @param apiType          三级部门
     * @return 查询结果
     */
    @ApiOperation("查询行业模板接口")
    @PostMapping(value = "/queryAllTemplate")
    public ApiResult<PageResultReturn<MarketingIndustryTemplate>> queryAllTemplate(@RequestParam(name = "current") Integer current,
                                                                                   @RequestParam(name = "pageSize") Integer pageSize,
                                                                                   @RequestParam(name = "templateName", required = false) String templateName,
                                                                                   @RequestParam(name = "firstDepartment", required = false) String firstDepartment,
                                                                                   @RequestParam(name = "secondDepartment", required = false) String secondDepartment,
                                                                                   @RequestParam(name = "apiType", required = false) String apiType) {
        try {
            Result<PageResultReturn<MarketingIndustryTemplate>> result = templateService.queryAllTemplate(current, pageSize, templateName, firstDepartment, secondDepartment, apiType);
            if (result.isSuccess()) {
                return new ApiResult<PageResultReturn<MarketingIndustryTemplate>>().success().setData(result.getData());
            } else {
                return new ApiResult<PageResultReturn<MarketingIndustryTemplate>>().fail().setMessage(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("查询行业模板异常,message:{}", e.getMessage());
            return new ApiResult<PageResultReturn<MarketingIndustryTemplate>>().fail().setMessage(e.getMessage()).setData(null);
        }
    }

    /**
     * 修改行业模板接口
     *
     * @param marketingIndustryTemplateDTO 行业模板信息
     * @return 修改结果
     */
    @ApiOperation("修改行业模板接口")
    @PostMapping(value = "/editTemplate")
    public ApiResult<Boolean> editTemplate(@RequestBody MarketingIndustryTemplateDTO marketingIndustryTemplateDTO) {
        try {
            Result<Boolean> result = templateService.editTemplate(marketingIndustryTemplateDTO);
            if (result.isSuccess()) {
                return new ApiResult<Boolean>().success().setData(result.getData());
            } else {
                return new ApiResult<Boolean>().fail().setMessage(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("修改行业模板异常,message:{}", e.getMessage());
            return new ApiResult<Boolean>().fail().setMessage(e.getMessage()).setData(null);
        }
    }

    /**
     * 删除行业模板接口
     *
     * @param id 模板id
     * @return 删除结果
     */
    @ApiOperation("删除行业模板接口")
    @PostMapping(value = "/deleteTemplate")
    public ApiResult<Boolean> deleteTemplate(@RequestParam(name = "id") Long id) {
        try {
            Result<Boolean> result = templateService.deleteTemplate(id);
            if (result.isSuccess()) {
                return new ApiResult<Boolean>().success().setData(result.getData());
            } else {
                return new ApiResult<Boolean>().fail().setMessage(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("删除行业模板异常,message:{}", e.getMessage());
            return new ApiResult<Boolean>().fail().setMessage(e.getMessage()).setData(null);
        }
    }

    @ApiOperation("根据id查询行业模板")
    @PostMapping(value = "/queryTemplateById")
    public ApiResult<MarketingIndustryTemplateDTO> queryTemplateById(@RequestParam(name = "id") Long id) {
        try {
            Result<MarketingIndustryTemplateDTO> result = templateService.queryTemplateById(id);
            if (result.isSuccess()) {
                return new ApiResult<MarketingIndustryTemplateDTO>().success().setData(result.getData());
            } else {
                return new ApiResult<MarketingIndustryTemplateDTO>().fail().setMessage(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("根据id查询行业模板异常,message:{}", e.getMessage());
            return new ApiResult<MarketingIndustryTemplateDTO>().fail().setMessage(e.getMessage()).setData(null);
        }

    }

    @ApiOperation("根据三级部门及数据类型查询行业模板")
    @PostMapping(value = "/queryTemplateJsonParse")
    public ApiResult<JSONArray> queryTemplateJsonParse(@RequestParam(name = "firstDepartment") String firstDepartment,
                                                       @RequestParam(name = "secondDepartment") String secondDepartment,
                                                       @RequestParam(name = "apiType") String apiType,
                                                       @RequestParam(name = "dateType") Integer dataType) {
        try {
            Result<JSONArray> result = industryTemplateJsonParseService.queryIndustryTemplateJsonParses(firstDepartment, secondDepartment, apiType, dataType);
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
