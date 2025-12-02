package com.br.marketing.innerapi.controller;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.dto.DataExportTaskDTO;
import com.br.marketing.dto.ExecuteCarClueDTO;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.carclue.web.CarClueReportService;
import com.br.marketing.vo.CarClueInfoVo;
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
 * 车线索列表
 * @author guangxiu.li
 * @date 2025/1/14
 * @description
 */
@RestController
@RequestMapping(value = "/car")
@Tag(name = "车线索列表", description = "车线索列表")
public class CarClueReportController {

    @Resource
    private CarClueReportService carClueReportService;

    private static final Logger log = LoggerFactory.getLogger(CarClueReportController.class);



    @PostMapping("/getCarClueList")
    @Operation(summary = "车线索数据统计报表列表", description = "车线索数据统计报表列表")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getReportList(@RequestBody @Valid CarClueReportDTO request) {
        PageResultReturn result = carClueReportService.getReportList(request);
        if (result != null) {
            return new ApiResult<PageResultReturn>().success(result);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }


    @Operation(summary = "批量编辑车线索信息", description = "批量编辑车线索信息")
    @PostMapping("/editCarClues")
    public ApiResult<Boolean> editCarClues(@RequestBody @Validated List<CarClueInfo> voList) {
        try {
            return carClueReportService.editCarClues(voList);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "批量编辑车线索接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "增加线索执行记录", description = "增加线索执行记录")
    @PostMapping("/executeClueData")
    public ApiResult<Boolean> executeClueData(@RequestBody @Validated ExecuteCarClueDTO dto) {
        try {
            MarketingUserDetail user = ThreadContextInfo.getUser();
            return carClueReportService.executeClueData(dto,user);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "增加线索执行记录接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/createTask")
    public ApiResult<Boolean> createTask(@RequestBody @Valid DataExportTaskDTO taskDTO) {
        try {
            MarketingUserDetail user = ThreadContextInfo.getUser();
            return carClueReportService.createTask(taskDTO, user);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BAOXIAN_SERVICEERROR.getCode(),
                    "创建导出任务错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

}
