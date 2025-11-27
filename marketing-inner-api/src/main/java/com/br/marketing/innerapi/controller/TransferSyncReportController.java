package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.TransferSyncReportService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * 转化数据统计报表
 *
 * @author Guo Zeqiang
 * @dateTime 2022/6/30 21:04
 */
@RestController
@RequestMapping(value = "/rule/tsr")
@Tag(name = "客户转化数据统计报表", description = "客户转化数据统计报表")
@Slf4j
public class TransferSyncReportController {

    @Resource
    private TransferSyncReportService transferSyncReportService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @GetMapping("getReportList")
    @Operation(summary = "客户转化数据统计报表列表", description = "客户转化数据统计报表列表")
    @Parameters({@Parameter(name = "current", description = "页号")
            , @Parameter(name = "size", description = "页大小")
            , @Parameter(name = "cidOrName", description = "客户名称/客户编号")
            , @Parameter(name = "appletTimeStart", description = "上传日期开始")
            , @Parameter(name = "appletTimeEnd", description = "上传日期截至")
            , @Parameter(name = "apiCodes", description = "apiCode筛选,支持多选,逗号分隔")
            , @Parameter(name = "userTypes", description = "场景筛选,支持多选,逗号分隔(例：S01,S02,促首登)")
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getReportList(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String cidOrName
            , @RequestParam(required = false) String appletTimeStart
            , @RequestParam(required = false) String appletTimeEnd
            , @RequestParam(required = false) String apiCodes
            , @RequestParam(required = false) String userTypes) {
        PageResultReturn listPage = transferSyncReportService.getTransferSyncReportList(current, size, cidOrName, appletTimeStart, appletTimeEnd, apiCodes, userTypes);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    @GetMapping("getReportListTotal")
    @Operation(summary = "客户转化数据统计报表总计", description = "客户转化数据统计报表列表总计")
    @Parameters({@Parameter(name = "cidOrName", description = "客户名称/客户编号")
            , @Parameter(name = "appletTimeStart", description = "上传日期开始")
            , @Parameter(name = "appletTimeEnd", description = "上传日期截至")
            , @Parameter(name = "apiCodes", description = "apiCode筛选,支持多选,逗号分隔")
            , @Parameter(name = "userTypes", description = "场景筛选,支持多选,逗号分隔(例：S01,S02,促首登)")
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    @AddDataAuthBusiness
    public ApiResult<Map<String, String>> getReportListTotal(@RequestParam(required = false) String cidOrName
            , @RequestParam(required = false) String appletTimeStart
            , @RequestParam(required = false) String appletTimeEnd
            , @RequestParam(required = false) String apiCodes
            , @RequestParam(required = false) String userTypes) {
        Map<String, String> map = transferSyncReportService.getTransferSyncReportListTotal(cidOrName, appletTimeStart, appletTimeEnd, apiCodes, userTypes);
        return new ApiResult<Map<String, String>>().success(map);
    }

    @GetMapping("triggerTaskReportJob")
    @Operation(summary = "手动执行转化数据统计报表任务", description = "手动执行转化数据统计报表任务")
    @Parameter(name = "uploadDate", description = "当日日期(yyyy-MM-dd)")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    public ApiResult<Boolean> triggerTaskUploadSyncReportJob(@RequestParam(required = false) String dateStr) {
        try {
            boolean statisSwitch = !marketingCommonConfig.getUploadAndTransferDataRealtimeStatisSwitch();
            if (statisSwitch) {
                transferSyncReportService.reportProcess(new HashSet<>(Collections.singletonList(StringUtils.isBlank(dateStr)
                        ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) : dateStr)));
            }
            return new ApiResult<Boolean>().success(Boolean.TRUE);
        } catch (Exception e) {
            log.warn("手动执行转化数据统计报表任务异常");
            return new ApiResult<Boolean>().fail("手动执行转化数据统计报表任务异常,请稍后再试！");
        }
    }
}
