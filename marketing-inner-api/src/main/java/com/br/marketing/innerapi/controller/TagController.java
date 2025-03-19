package com.br.marketing.innerapi.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tag.*;
import com.br.marketing.service.tag.web.TagService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.ConnectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 标签配置管理
 * @author your.name
 * @date 2024/1/x
 * @description
 */
@RestController
@RequestMapping(value = "/tag")
@Api(value = "标签配置管理", tags = "标签配置管理", produces = "application/json", consumes = "application/json", protocols = "http")
public class TagController {

    @Resource
    private TagService tagService;

    private static final Logger log = LoggerFactory.getLogger(TagController.class);

    @PostMapping("/getTagList")
    @ApiOperation(value = "获取标签列表", notes = "分页获取标签列表信息")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "request", value = "查询参数", required = true, dataType = "TagQueryDTO")
    })
    public ApiResult<PageResultReturn> getTagList(@RequestBody @Valid TagQueryDTO request) {
        try {
            PageResultReturn result = tagService.getTagList(request);
            return new ApiResult<PageResultReturn>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取标签列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/createTag")
    @ApiOperation(value = "创建标签", notes = "创建标签")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = Boolean.class)})
    public ApiResult<Boolean> createTag(@RequestBody @Validated TagCreateDTO request) {
        try {
            String tagCode = tagService.createTag(request);
            if (tagCode != null) {
                return new ApiResult<Boolean>().success(true);
            }
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "创建标签接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/updateTag")
    @ApiOperation(value = "更新标签", notes = "更新标签")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = Boolean.class)})
    public ApiResult<Boolean> updateTag(@RequestBody @Validated TagUpdateDTO request) {
        try {
            return tagService.updateTag(request);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "更新标签接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/updateTagStatus")
    @ApiOperation(value = "更新标签状态", notes = "更新标签启用/禁用状态")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "tagCode", value = "标签编码", required = true, dataType = "String"),
        @ApiImplicitParam(name = "status", value = "状态（true-启用，false-禁用）", required = true, dataType = "Boolean")
    })
    public ApiResult<Boolean> updateTagStatus(
            @RequestParam String tagCode,
            @RequestParam Boolean status) {
        try {
            return tagService.updateTagStatus(tagCode, status);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "更新标签状态接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/getFieldConfigs")
    @ApiOperation(value = "获取字段配置", notes = "获取字段配置")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = TagFieldConfigDTO.class)})
    public ApiResult<List<TagFieldConfigDTO>> getFieldConfigs(
            @ApiParam("API编码") @RequestParam String apiCode) {
        try {
            List<TagFieldConfigDTO> configs = tagService.getFieldConfigs(apiCode);
            if (configs != null) {
                return new ApiResult<List<TagFieldConfigDTO>>().success(configs);
            }
            return new ApiResult<List<TagFieldConfigDTO>>().fail(ServiceResultEnum.FAILED);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取字段配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<TagFieldConfigDTO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/getApiCodes")
    @ApiOperation(value = "获取API编码列表", notes = "获取API编码列表")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = String.class)})
    public ApiResult<List<String>> getApiCodes() {
        try {
            List<String> apiCodes = tagService.getApiCodes();
            if (apiCodes != null) {
                return new ApiResult<List<String>>().success(apiCodes);
            }
            return new ApiResult<List<String>>().fail(ServiceResultEnum.FAILED);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取API编码列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<String>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/getTagLibrary")
    @ApiOperation(value = "同步标签库", notes = "同步标签库")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = String.class)})
    public ApiResult<List<String>> getLabels() {
        try {
            List<String> tagList = tagService.getTagLibrary();
            if (!CollectionUtil.isEmpty(tagList)) {
                return new ApiResult<List<String>>().success(tagList);
            }
            return new ApiResult<List<String>>().fail(ServiceResultEnum.FAILED);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "同步标签接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<String>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/batchDelete")
    @ApiOperation(value = "批量删除标签", notes = "批量删除标签")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "request", value = "删除参数", required = true, dataType = "TagBatchDeleteDTO")
    })
    public ApiResult<Boolean> batchDelete(@RequestBody @Valid TagBatchDeleteDTO request) {
        try {
            return tagService.batchDelete(request);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "批量删除标签接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getCreators")
    @ApiOperation(value = "获取创建人列表", notes = "获取标签创建人列表")
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

}