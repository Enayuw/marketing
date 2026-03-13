package com.br.marketing.innerapi.controller.autocheck;

import cn.hutool.core.date.DateUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.dto.autocheck.BatchInitAutoCheckSceneDictDto;
import com.br.marketing.dto.autocheck.BatchInitAutoCheckTableDictDto;
import com.br.marketing.dto.autocheck.QueryAssociationTableFieldDto;
import com.br.marketing.dto.autocheck.SaveAutoCheckConfigDto;
import com.br.marketing.dto.autocheck.SaveAutoCheckConfigResDto;
import com.br.marketing.service.autocheck.AutoCheckService;
import com.br.marketing.vo.autocheck.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 自动化巡检
 *
 * @author fuzhen.zhang
 * @dateTime 2025/12/18 15:12
 */
@RestController
@RequestMapping(value = "/auto/check")
@Tag(name = "自动化巡检", description = "自动化巡检")
public class AutoCheckController {
    private static final Logger log = LoggerFactory.getLogger(AutoCheckController.class);

    @Resource
    private AutoCheckService autoCheckService;

    @GetMapping("/configList")
    @Operation(summary = "根据指定的apiCode和场景查询已有配置", description = "根据指定的apiCode和场景查询已有配置")
    @Parameters({@Parameter(name = "apiCodes", description = "多apiCode用逗号分隔"),
            @Parameter(name = "sceneCodes", description = "场景编码，多场景逗号分隔")
    })
    public ApiResult<List<AutoCheckConfigVO>> getAutoCheckConfigList(@RequestParam(name = "apiCodes", required = false) String apiCodes,
                                                                     @RequestParam(name = "sceneCodes", required = false) String sceneCodes) {
        try {
            List<AutoCheckConfigVO> list = autoCheckService.getAutoCheckConfigList(apiCodes, sceneCodes);
            return new ApiResult<List<AutoCheckConfigVO>>().success(list);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "根据指定的apiCode和场景查询已有配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<AutoCheckConfigVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/sceneList")
    @Operation(summary = "场景下拉列表", description = "场景下拉列表")
    @Parameter(name = "searchContent", description = "场景编码或场景名称")
    public ApiResult<List<AutoCheckSceneVO>> getAutoCheckSceneList(@RequestParam(name = "searchContent", required = false) String searchContent) {
        try {
            List<AutoCheckSceneVO> list = autoCheckService.getAutoCheckSceneList(searchContent);
            return new ApiResult<List<AutoCheckSceneVO>>().success(list);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "场景下拉列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<AutoCheckSceneVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/save")
    @Operation(summary = "保存自动化巡检配置接口(新增/编辑)", description = "保存自动化巡检配置接口(新增/编辑)")
    public ApiResult<Boolean> saveAutoCheckConfig(@Valid @RequestBody SaveAutoCheckConfigDto dto) {
        try {
            SaveAutoCheckConfigResDto resDto = autoCheckService.saveAutoCheckConfig(dto);
            if (resDto.getRes()) {
                return new ApiResult<Boolean>().success(true);
            } else {
                return new ApiResult<Boolean>().fail(resDto.getCode()).
                        setData(resDto.getRes()).setMessage(resDto.getMessage());
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "保存自动化巡检配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/delete")
    @Operation(summary = "删除自动化巡检配置接口", description = "删除自动化巡检配置接口")
    @Parameters({@Parameter(name = "apiCode", description = "apiCode"),
            @Parameter(name = "sceneCode", description = "场景编码")
    })
    public ApiResult<Boolean> delAutoCheckConfig(@RequestParam(name = "apiCode") String apiCode,
                                                 @RequestParam(name = "sceneCode") String sceneCode) {
        try {
            Boolean res = autoCheckService.delAutoCheckConfig(apiCode, sceneCode);
            return new ApiResult<Boolean>().success(res);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "删除自动化巡检配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/resultList")
    @Operation(summary = "根据apiCode和场景查询巡检结果", description = "根据apiCode和场景查询巡检结果")
    @Parameters({@Parameter(name = "apiCodes", description = "多apiCode用逗号分隔"),
            @Parameter(name = "sceneCodes", description = "场景编码，多场景逗号分隔")
    })
    public ApiResult<List<AutoCheckResultVO>> getResultList(@RequestParam(name = "apiCodes", required = false) String apiCodes,
                                                            @RequestParam(name = "sceneCodes", required = false) String sceneCodes,
                                                            @RequestParam(name = "startTime", required = false) String startTime,
                                                            @RequestParam(name = "endTime", required = false) String endTime) {
        try {
            // 默认当天比对结果
            if (StringUtils.isBlank(startTime) && StringUtils.isBlank(endTime)) {
                String today = DateUtil.today();
                startTime = today + " 00:00:00";
                endTime = today + " 23:59:59";
            }
            List<AutoCheckResultVO> list = autoCheckService.getResultList(apiCodes, sceneCodes, startTime, endTime);
            return new ApiResult<List<AutoCheckResultVO>>().success(list);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "根据指定的apiCode和场景查询已有配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<AutoCheckResultVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/associationTable")
    @Operation(summary = "查询关联表接口", description = "查询关联表接口")
    @Parameter(name = "tableName", description = "tableName")
    public ApiResult<List<AutoCheckAssociationTableVO>> getAssociationTable(@RequestParam(name = "tableName", required = false) String tableName) {
        try {
            List<AutoCheckAssociationTableVO> list = autoCheckService.getAssociationTable(tableName);
            return new ApiResult<List<AutoCheckAssociationTableVO>>().success(list);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "查询关联表接口！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<AutoCheckAssociationTableVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/table/field")
    @Operation(summary = "根据关联表查询表字段接口（批量）", description = "根据关联表查询表字段接口（批量）")
    public ApiResult<List<AutoCheckAssociationTableFieldVO>> getAssociationTableFields(@RequestBody QueryAssociationTableFieldDto dto) {
        try {
            List<AutoCheckAssociationTableFieldVO> list = autoCheckService.getAssociationTableFields(dto);
            return new ApiResult<List<AutoCheckAssociationTableFieldVO>>().success(list);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "根据关联表查询表字段接口！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<AutoCheckAssociationTableFieldVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/dict/scene/initBatch")
    @Operation(summary = "初始化/维护场景字典（批量）", description = "用于后端维护数据：批量初始化 b_auto_check_scene_dict（按 sceneCode 幂等写入）")
    public ApiResult<AutoCheckDictInitResultVO> initSceneDictBatch(@Valid @RequestBody BatchInitAutoCheckSceneDictDto dto) {
        try {
            AutoCheckDictInitResultVO res = autoCheckService.initSceneDictBatch(dto);
            return new ApiResult<AutoCheckDictInitResultVO>().success(res);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "初始化/维护场景字典（批量）接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<AutoCheckDictInitResultVO>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/dict/table/initBatch")
    @Operation(summary = "初始化/维护关联表字典（批量）", description = "用于后端维护数据：批量初始化 b_auto_check_table_dict（按 tableName 幂等写入）")
    public ApiResult<AutoCheckDictInitResultVO> initTableDictBatch(@Valid @RequestBody BatchInitAutoCheckTableDictDto dto) {
        try {
            AutoCheckDictInitResultVO res = autoCheckService.initTableDictBatch(dto);
            return new ApiResult<AutoCheckDictInitResultVO>().success(res);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "初始化/维护关联表字典（批量）接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<AutoCheckDictInitResultVO>().fail(ServiceResultEnum.FAILED);
        }
    }
}
