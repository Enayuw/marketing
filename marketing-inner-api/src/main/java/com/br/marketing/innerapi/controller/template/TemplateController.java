package com.br.marketing.innerapi.controller.template;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.template.MarketingIndustryTemplateDTO;
import com.br.marketing.entity.MarketingIndustryTemplate;
import com.br.marketing.service.template.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "行业模板相关接口", description = "行业模板相关接口")
public class TemplateController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @Resource
    private TemplateService templateService;

    /**
     * 新增行业模板
     *
     * @param marketingIndustryTemplateDTO 模板信息
     * @return 是否新增成功
     */
    @Operation(summary = "新增行业模板", description = "新增行业模板")
    @PostMapping(value = "/saveOrUpdateTemplate")
    public ApiResult<Boolean> saveOrUpdateTemplate(@RequestBody MarketingIndustryTemplateDTO marketingIndustryTemplateDTO) {
        try {
            Result<Boolean> result;
            if (StringUtils.isNotBlank(String.valueOf(marketingIndustryTemplateDTO.getMarketingIndustryTemplate().getId()))) {
                result = templateService.editTemplate(marketingIndustryTemplateDTO);
            } else {
                result = templateService.addTemplate(marketingIndustryTemplateDTO);
            }
            return new ApiResult<Boolean>().fromResult(result, 1);
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
    @Operation(summary = "查询行业模板", description = "查询行业模板接口")
    @Parameters({
            @Parameter(name = "current", description = "页号"),
            @Parameter(name = "pageSize", description = "页大小"),
            @Parameter(name = "templateName", description = "行业模板名称"),
            @Parameter(name = "firstDepartment", description = "一级部门"),
            @Parameter(name = "secondDepartment", description = "二级部门"),
            @Parameter(name = "apiType", description = "三级部门"),
            @Parameter(name = "systemType", description = "数据来源"),
            @Parameter(name = "dataType", description = "接口用途")
    })
    @PostMapping(value = "/queryAllTemplate")
    public ApiResult<PageResultReturn<MarketingIndustryTemplate>> queryAllTemplate(@RequestParam(name = "current") Integer current
            , @RequestParam(name = "pageSize") Integer pageSize
            , @RequestParam(name = "templateName", required = false) String templateName
            , @RequestParam(name = "firstDepartment", required = false) String firstDepartment
            , @RequestParam(name = "secondDepartment", required = false) String secondDepartment
            , @RequestParam(name = "apiType", required = false) String apiType
            , @RequestParam(name = "systemType", required = false) Integer systemType
            , @RequestParam(name = "dataType", required = false) Integer dataType) {
        try {
            Result<PageResultReturn<MarketingIndustryTemplate>> result =
                    templateService.queryAllTemplate(current, pageSize, templateName,
                            firstDepartment, secondDepartment, apiType, systemType, dataType);
            return new ApiResult<PageResultReturn<MarketingIndustryTemplate>>().fromResult(result, 1);
        } catch (Exception e) {
            logger.error("查询行业模板异常,message:{}", e.getMessage());
            return new ApiResult<PageResultReturn<MarketingIndustryTemplate>>().fail().setMessage(e.getMessage()).setData(null);
        }
    }



    /**
     * 删除行业模板接口
     *
     * @param id 模板id
     * @return 删除结果
     */
    @Operation(summary = "删除行业模板", description = "删除行业模板")
    @GetMapping(value = "/deleteTemplate")
    public ApiResult<Boolean> deleteTemplate(@RequestParam(name = "id") Long id) {
        try {
            Result<Boolean> result = templateService.deleteTemplate(id);
            return new ApiResult<Boolean>().fromResult(result, 1);
        } catch (Exception e) {
            logger.error("删除行业模板异常,message:{}", e.getMessage());
            return new ApiResult<Boolean>().fail().setMessage(e.getMessage()).setData(null);
        }
    }

    @Operation(summary = "根据id查询行业模板", description = "根据id查询行业模板")
    @GetMapping(value = "/queryTemplateById")
    public ApiResult<MarketingIndustryTemplateDTO> queryTemplateById(@RequestParam(name = "id") Long id) {
        try {
            Result<MarketingIndustryTemplateDTO> result = templateService.queryTemplateById(id);
            return new ApiResult<MarketingIndustryTemplateDTO>().fromResult(result, 1);
        } catch (Exception e) {
            logger.error("根据id查询行业模板异常,message:{}", e.getMessage());
            return new ApiResult<MarketingIndustryTemplateDTO>().fail().setMessage(e.getMessage()).setData(null);
        }

    }

}
