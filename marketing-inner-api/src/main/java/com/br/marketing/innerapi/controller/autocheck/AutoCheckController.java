package com.br.marketing.innerapi.controller.autocheck;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.service.autocheck.AutoCheckService;
import com.br.marketing.vo.autocheck.AutoConfigVO;
import com.br.marketing.vo.autocheck.SenceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 自动化巡检
 *
 * @author fuzhen.zhang
 * @dateTime 2025/12/18 15:12
 */
@RestController
@RequestMapping(value = "/auto/check")
@Tag(name = "客户信息", description = "自动化巡检")
public class AutoCheckController {
    private static final Logger log = LoggerFactory.getLogger(AutoCheckController.class);

    @Resource
    private AutoCheckService autoCheckService;

    @GetMapping("/configList")
    @Operation(summary = "根据指定的apiCode和场景查询已有配置", description = "根据指定的apiCode和场景查询已有配置")
    @Parameters({@Parameter(name = "apiCodes", description = "多apiCode用逗号分隔"),
            @Parameter(name = "senceCodes", description = "场景编码，多场景逗号分隔")
    })
    public ApiResult<List<AutoConfigVO>> getConfigList(@RequestParam(name = "apiCodes") String apiCodes,
                                                       @RequestParam(name = "senceCodes") String senceCodes) {
        try {
            List<AutoConfigVO> list = autoCheckService.getConfigList(apiCodes, senceCodes);
            return new ApiResult<List<AutoConfigVO>>().success(list);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "根据指定的apiCode和场景查询已有配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<AutoConfigVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/senceList")
    @Operation(summary = "场景下拉列表", description = "场景下拉列表")
    @Parameter(name = "searchContent", description = "场景编码或场景名称")
    public ApiResult<List<SenceVO>> searchSenceList(@RequestParam(name = "searchContent") String searchContent) {
        try {
            List<SenceVO> list = autoCheckService.searchSenceList(searchContent);
            return new ApiResult<List<SenceVO>>().success(list);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "场景下拉列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<SenceVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

}
