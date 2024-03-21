package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.TransferSyncReportService;
import io.swagger.annotations.*;
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
@Api(value = "客户转化数据统计报表", tags = "客户转化数据统计报表", produces = "application/json", consumes = "application/json", protocols = "http")
@Slf4j
public class TransferSyncReportController {

    @Resource
    private TransferSyncReportService transferSyncReportService;

    @GetMapping("getReportList")
    @ApiOperation(value = "客户转化数据统计报表列表", notes = "客户转化数据统计报表列表", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "cidOrName", value = "客户名称/客户编号", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "appletTimeStart", value = "上传日期开始", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "appletTimeEnd", value = "上传日期截至", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "apiCodes", value = "apiCode筛选,支持多选,逗号分隔", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "userTypes", value = "场景筛选,支持多选,逗号分隔(例：S01,S02,促首登)", paramType = "query", dataType = "string")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingSyncReport.class)})
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
    @ApiOperation(value = "客户转化数据统计报表总计", notes = "客户转化数据统计报表列表总计", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "cidOrName", value = "客户名称/客户编号", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "appletTimeStart", value = "上传日期开始", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "appletTimeEnd", value = "上传日期截至", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "apiCodes", value = "apiCode筛选,支持多选,逗号分隔", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "userTypes", value = "场景筛选,支持多选,逗号分隔(例：S01,S02,促首登)", paramType = "query", dataType = "string")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingSyncReport.class)})
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
    @ApiOperation(value = "手动执行转化数据统计报表任务", notes = "手动执行转化数据统计报表任务", httpMethod = "GET")
    @ApiImplicitParam(name = "uploadDate", value = "当日日期(yyyy-MM-dd)", paramType = "query", dataType = "string")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingSyncReport.class)})
    public ApiResult<Boolean> triggerTaskUploadSyncReportJob(@RequestParam(required = false) String dateStr
            , @RequestParam(required = false, defaultValue = "false") Boolean isManual) {
        try {
            if (isManual) {
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
