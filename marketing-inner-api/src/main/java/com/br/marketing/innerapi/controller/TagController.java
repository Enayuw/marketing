package com.br.marketing.innerapi.controller;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.tag.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.tag.web.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 标签配置管理
 * @author guangxiu.li
 * @date 2025/03/18
 * @description
 */
@RestController
@RequestMapping(value = "/tag")
@Tag(name = "标签配置管理", description = "标签配置管理")
public class TagController {

    @Resource
    private TagService tagService;

    private static final Logger log = LoggerFactory.getLogger(TagController.class);

    @PostMapping("/getTagList")
    @Operation(summary = "获取标签列表", description = "分页获取标签列表信息")
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getTagList(@RequestBody @Valid TagQueryDTO request) {
        try {
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            request.setCurrentUserId(Long.valueOf(userDetail.getId()));
            PageResultReturn result = tagService.getTagList(request);
            return new ApiResult<PageResultReturn>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取标签列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/createTag")
    @Operation(summary = "创建标签", description = "创建标签")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    public ApiResult<Boolean> createTag(@RequestBody @Validated TagCreateDTO request) {
        try {
            Boolean result = tagService.createTag(request);
            return new ApiResult<Boolean>().success(result);
        } catch (BusinessException be) {
            // 处理业务异常
            return new ApiResult<Boolean>().fail(false, be.getMsg());
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "创建标签接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/updateTag")
    @Operation(summary = "更新标签", description = "更新标签")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    @AddDataAuthBusiness
    public ApiResult<Boolean> updateTag(@RequestBody @Validated TagUpdateDTO request) {
        try {
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            request.setOptUserId(Long.valueOf(userDetail.getId()));
            request.setOptUserName(userDetail.getUserName());
            Boolean result = tagService.updateTag(request);
            return new ApiResult<Boolean>().success(result);
        } catch (BusinessException be) {
            return new ApiResult<Boolean>().fail(false, be.getMsg());
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "更新标签接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/updateTagStatus")
    @Operation(summary = "更新标签状态", description = "更新标签启用/禁用状态")
    public ApiResult<Boolean> updateTagStatus(@RequestBody UpdateTagStatusDTO dto) {
        try {
            Boolean result = tagService.updateTagStatus(dto.getTagCode(), dto.getStatus());
            return new ApiResult<Boolean>().success(result);
        } catch (BusinessException be) {
            return new ApiResult<Boolean>().fail(false, be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "更新标签状态失败！错误信息：" + e.getMessage()), e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }


    @PostMapping("/getFieldConfigs")
    @Operation(summary = "获取字段配置", description = "根据数据源编码获取对应的字段配置信息")
    public ApiResult<List<TagFieldConfigDTO>> getFieldConfigs(@RequestParam String sourceCode) {
        try {
            List<TagFieldConfigDTO> configs = tagService.getFieldConfigs(sourceCode);
            return configs != null ? new ApiResult<List<TagFieldConfigDTO>>().success(configs)
                    : new ApiResult<List<TagFieldConfigDTO>>().fail(ServiceResultEnum.FAILED);
        } catch (BusinessException be) {
            return new ApiResult<List<TagFieldConfigDTO>>().fail(be.getMsg());
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取字段配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<TagFieldConfigDTO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/getValueOptions")
    @Operation(summary = "获取字段值列表", description = "根据数据源编码获取对应的字段配置信息")
    @Parameters({
            @Parameter(name = "fieldCode", description = "字段编码", required = true, example = "SOURCE_001")
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    public ApiResult<List<String>> getValueOptions(
            @Parameter(description = "字段编码", required = true) @RequestParam String fieldCode) {
        try {
            List<String> configs = tagService.getValueOptions(fieldCode);
            if (configs != null) {
                return new ApiResult<List<String>>().success(configs);
            }
            return new ApiResult<List<String>>().fail(ServiceResultEnum.FAILED);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取字段配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<String>>().fail(ServiceResultEnum.FAILED);
        }
    }


    @PostMapping("/getEffectiveTag")
    @Operation(summary = "获取apiCode授权标签", description = "获取apiCode授权标签")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    public ApiResult<List<TagEffectiveDTO>> getEffectiveTag(@Parameter(name = "apiCode") @RequestParam String apiCode) {
        try {
            List<TagEffectiveDTO> tagList = tagService.getEffectiveTag(apiCode);
            if (tagList != null) {
                return new ApiResult<List<TagEffectiveDTO>>().success(tagList);
            }
            return new ApiResult<List<TagEffectiveDTO>>().fail(ServiceResultEnum.FAILED);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取apiCode授权标签接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<TagEffectiveDTO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/batchDelete")
    @Operation(summary = "批量删除标签", description = "批量删除标签")
    @AddDataAuthBusiness
    public ApiResult<Boolean> batchDelete(@RequestBody @Valid TagBatchDeleteDTO request) {
        try {
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            request.setCurrentUserId(Long.valueOf(userDetail.getId()));
            Boolean result = tagService.batchDelete(request);
            return new ApiResult<Boolean>().success(result);
        } catch (BusinessException be) {
            return new ApiResult<Boolean>().fail(false, be.getMsg());
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "批量删除标签接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getCreators")
    @Operation(summary = "获取创建人列表", description = "获取标签创建人列表")
    public ApiResult<List<TagCreatorDTO>> getCreators() {
        try {
            List<TagCreatorDTO> creators = tagService.getCreators();
            return new ApiResult<List<TagCreatorDTO>>().success(creators);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取创建人列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<TagCreatorDTO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getTagName")
    @Operation(summary = "获取标签名称列表", description = "获取标签名称列表")
    public ApiResult<List<TagListResponseDTO>> getTagName() {
        try {
            List<TagListResponseDTO> creators = tagService.getTagName();
            return new ApiResult<List<TagListResponseDTO>>().success(creators);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取创建人列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<TagListResponseDTO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getTagDetail")
    @Operation(summary = "获取标签详情", description = "根据标签编码获取标签详细信息，用于编辑前的数据反显")
    public ApiResult<TagDetailDTO> getTagDetail(@RequestParam Long id) {
        try {
            TagDetailDTO detail = tagService.getTagDetail(id);
            return new ApiResult<TagDetailDTO>().success(detail);
        } catch (BusinessException be) {
            return new ApiResult<TagDetailDTO>().fail(be.getMsg());
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取标签详情失败！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<TagDetailDTO>().fail(ServiceResultEnum.FAILED);
        }
    }


}