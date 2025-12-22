package com.br.marketing.innerapi.controller.autocheck;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.dto.autocheck.SaveAutoCheckConfigDto;
import com.br.marketing.service.autocheck.AutoCheckService;
import com.br.marketing.vo.autocheck.AutoCheckResultVO;
import com.br.marketing.vo.autocheck.AutoCheckConfigVO;
import com.br.marketing.vo.autocheck.AutoCheckSenceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            @Parameter(name = "senceCodes", description = "场景编码，多场景逗号分隔")
    })
    public ApiResult<List<AutoCheckConfigVO>> getAutoCheckConfigList(@RequestParam(name = "apiCodes", required = false) String apiCodes,
                                                                     @RequestParam(name = "senceCodes", required = false) String senceCodes) {
        try {
            List<AutoCheckConfigVO> list = autoCheckService.getAutoCheckConfigList(apiCodes, senceCodes);
            return new ApiResult<List<AutoCheckConfigVO>>().success(list);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "根据指定的apiCode和场景查询已有配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<AutoCheckConfigVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/senceList")
    @Operation(summary = "场景下拉列表", description = "场景下拉列表")
    @Parameter(name = "searchContent", description = "场景编码或场景名称")
    public ApiResult<List<AutoCheckSenceVO>> getAutoCheckSenceList(@RequestParam(name = "searchContent", required = false) String searchContent) {
        try {
            List<AutoCheckSenceVO> list = autoCheckService.getAutoCheckSenceList(searchContent);
            return new ApiResult<List<AutoCheckSenceVO>>().success(list);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "场景下拉列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<AutoCheckSenceVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/save")
    @Operation(summary = "保存自动化巡检配置接口(新增/编辑)", description = "保存自动化巡检配置接口(新增/编辑)")
    public ApiResult<Boolean> saveAutoCheckConfig(@Valid @RequestBody SaveAutoCheckConfigDto dto) {
        try {
            Boolean res = autoCheckService.saveAutoCheckConfig(dto);
            return new ApiResult<Boolean>().success(res);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "保存自动化巡检配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/delete")
    @Operation(summary = "删除自动化巡检配置接口", description = "删除自动化巡检配置接口")
    @Parameter(name = "id", description = "配置id")
    public ApiResult<Boolean> delAutoCheckConfig(@RequestParam(name = "id") Long id) {
        try {
            Boolean res = autoCheckService.delAutoCheckConfig(id);
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
            @Parameter(name = "senceCodes", description = "场景编码，多场景逗号分隔")
    })
    public ApiResult<List<AutoCheckResultVO>> getResultList(@RequestParam(name = "apiCodes", required = false) String apiCodes,
                                                            @RequestParam(name = "senceCodes", required = false) String senceCodes) {
        try {
            List<AutoCheckResultVO> list = autoCheckService.getResultList(apiCodes, senceCodes);
            return new ApiResult<List<AutoCheckResultVO>>().success(list);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "根据指定的apiCode和场景查询已有配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<AutoCheckResultVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

}
