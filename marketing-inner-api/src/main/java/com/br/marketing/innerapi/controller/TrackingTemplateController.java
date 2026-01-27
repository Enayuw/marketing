package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.datamap.template.*;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.datamap.TrackingTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 链路模板配置管理控制器
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Slf4j
@RestController
@RequestMapping("/dataMap/template")
@Tag(name = "数据地图-模板管理", description = "链路模板配置管理")
public class TrackingTemplateController {

    @Resource
    private TrackingTemplateService trackingTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建模板", description = "创建链路模板（含节点和边）")
    @AddDataAuthBusiness
    public ApiResult<Long> createTemplate(@RequestBody @Validated CreateTemplateRequest request) {
        try {
            return trackingTemplateService.createTemplate(request);
        } catch (Exception e) {
            log.error("创建模板失败: templateName={}", request.getTemplateName(), e);
            return new ApiResult<Long>().fail("创建模板失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    @Operation(summary = "更新模板", description = "更新链路模板信息")
    @AddDataAuthBusiness
    public ApiResult<Boolean> updateTemplate(@RequestBody @Validated CreateTemplateRequest request) {
        try {
            return trackingTemplateService.updateTemplate(request);
        } catch (Exception e) {
            log.error("更新模板失败: templateId={}", request.getId(), e);
            return new ApiResult<Boolean>().fail("更新模板失败: " + e.getMessage());
        }
    }

    @GetMapping("/delete")
    @Operation(summary = "删除模板", description = "删除链路模板")
    @AddDataAuthBusiness
    public ApiResult<Boolean> deleteTemplate(@RequestParam List<Long> ids) {
        try {
            return trackingTemplateService.deleteTemplate(ids);
        } catch (Exception e) {
            log.error("删除模板失败: ids={}", ids, e);
            return new ApiResult<Boolean>().fail("删除模板失败: " + e.getMessage());
        }
    }

    @PostMapping("/list")
    @Operation(summary = "查询模板列表", description = "分页查询模板列表")
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getTemplateList(@RequestBody @Validated TemplateListRequest request) {
        try {
            PageResultReturn pageResultReturn = trackingTemplateService.selectTemplateList(request);
            return new ApiResult<PageResultReturn>().success(pageResultReturn);
        } catch (Exception e) {
            log.error("查询模板列表失败", e);
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/detail")
    @Operation(summary = "查询模板详情", description = "查询模板详情（含节点和边）")
    @AddDataAuthBusiness
    public ApiResult<TemplateDetailResponse> getTemplateDetail(@RequestParam Long id) {
        try {
            return trackingTemplateService.getTemplateDetail(id);
        } catch (Exception e) {
            log.error("查询模板详情失败: id={}", id, e);
            return new ApiResult<TemplateDetailResponse>().fail("查询模板详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/updateStatus")
    @Operation(summary = "启用/禁用模板", description = "更新模板状态")
    @AddDataAuthBusiness
    public ApiResult<Boolean> updateTemplateStatus(@RequestBody @Validated UpdateTemplateStatusRequest request) {
        try {
            return trackingTemplateService.updateTemplateStatus(request);
        } catch (Exception e) {
            log.error("更新模板状态失败", e);
            return new ApiResult<Boolean>().fail("更新模板状态失败: " + e.getMessage());
        }
    }
}
