package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.StringUtils;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.MarketingSyncReportService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 客户上传数据统计报表
 *
 * @author songjuanjuan
 * @dateTime 2021/11/18 15:12
 */
@RestController
@RequestMapping(value = "/rule/report")
@Tag(name = "客户上传数据统计报表", description = "客户上传数据统计报表")
public class SyncReportController {

    private static final Logger log = LoggerFactory.getLogger(SyncReportController.class);

    @Resource
    private MarketingSyncReportService marketingSyncReportService;

    @Resource
    private MarketingSyncReportService syncReportService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @PostMapping("/getReportList")
    @Operation(summary = "客户上传数据统计报表列表", description = "客户上传数据统计报表列表")
    @Parameters({@Parameter(name = "current", description = "页号")
            , @Parameter(name = "size", description = "页大小")
            , @Parameter(name = "cidOrName", value = "客户名称/客户编号", paramType = "query", dataType = "string")
            , @Parameter(name = "appletTimeStart", value = "上传日期开始", paramType = "query", dataType = "string")
            , @Parameter(name = "appletTimeEnd", value = "上传日期截至", paramType = "query", dataType = "string")
            , @Parameter(name = "apiCodes", value = "apiCode筛选,支持多选,逗号分隔", paramType = "query", dataType = "string")
            , @Parameter(name = "userTypes", value = "场景筛选,支持多选,逗号分隔(例：S01,S02,促首登)", paramType = "query", dataType = "string")
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
        PageResultReturn listPage = marketingSyncReportService.getReportList(current, size, cidOrName,appletTimeStart,appletTimeEnd,apiCodes,userTypes);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    @GetMapping("/exportData")
    @Operation(summary = "导出客户上传数据记录", description = "导出客户上传数据记录")
    @Parameters({@Parameter(name = "cidOrName", value = "客户名称/客户编号", paramType = "query", dataType = "string")
            , @Parameter(name = "appletTimeStart", value = "上传日期开始", paramType = "query", dataType = "string")
            , @Parameter(name = "appletTimeEnd", value = "上传日期截至", paramType = "query", dataType = "string")
            , @Parameter(name = "apiCodes", value = "apiCode筛选,支持多选,逗号分隔", paramType = "query", dataType = "string")
            , @Parameter(name = "userTypes", value = "场景筛选,支持多选,逗号分隔(例：S01,S02,促首登)", paramType = "query", dataType = "string")
            , @Parameter(name = "selectType", value = "选择类型(例：1全选,0:指定筛选)", paramType = "query", dataType = "integer",defaultValue = "1")
            , @Parameter(name = "selectExportIds", value = "选中要导出的id数据,逗号分隔(例：1,2,3,4)", paramType = "query", dataType = "string")
    })
    @AddDataAuthBusiness
    public void exportData(@RequestParam(required = false) String cidOrName
            , @RequestParam(required = false) String appletTimeStart
            , @RequestParam(required = false) String appletTimeEnd
            , @RequestParam(required = false) String apiCodes
            , @RequestParam(required = false) String userTypes
            , @RequestParam(defaultValue = "1") Integer selectType
            , @RequestParam(required = false) String selectExportIds
            , HttpServletResponse response) {
        try {
            marketingSyncReportService.exportData(cidOrName,appletTimeStart,appletTimeEnd,apiCodes,userTypes,selectType,selectExportIds,response);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SYNC_REPORT_EXPORT_SERVICEERROR.getCode(), e.getMessage()), e);
        }
    }

    @PostMapping("/getReportListTotal")
    @Operation(summary = "客户上传数据统计报表总计", description = "客户上传数据统计报表列表总计")
    @Parameters({@Parameter(name = "cidOrName", value = "客户名称/客户编号",paramType = "query", dataType = "string")
            , @Parameter(name = "appletTimeStart", value = "上传日期开始",paramType = "query", dataType = "string")
            , @Parameter(name = "appletTimeEnd",value = "上传日期截至", paramType = "query", dataType = "string")
            , @Parameter(name = "apiCodes", value = "apiCode筛选,支持多选,逗号分隔", paramType = "query", dataType = "string")
            , @Parameter(name = "userTypes", value = "场景筛选,支持多选,逗号分隔(例：S01,S02,促首登)", paramType = "query", dataType = "string")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingSyncReport.class)})
    @AddDataAuthBusiness
    public ApiResult<Map> getReportListTotal(@RequestParam(required = false) String cidOrName
            , @RequestParam(required = false) String appletTimeStart
            , @RequestParam(required = false) String appletTimeEnd
            , @RequestParam(required = false) String apiCodes
            , @RequestParam(required = false) String userTypes) {
        Map map = marketingSyncReportService.getReportListTotal(cidOrName,appletTimeStart,appletTimeEnd,apiCodes,userTypes);
        if (map != null) {
            return new ApiResult<Map>().success(map);
        }
        return new ApiResult<Map>().fail(ServiceResultEnum.FAILED);
    }

    @GetMapping("/triggerTaskUploadSyncReportJob")
    @Operation(summary = "手动执行上传数据统计报表任务", description = "手动执行上传数据统计报表任务")
    @Parameter(name = "uploadDate", value = "当日日期(yyyy-MM-dd)", paramType = "query", dataType = "string")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingSyncReport.class)})
    public ApiResult<Boolean> triggerTaskUploadSyncReportJob(@RequestParam(required = false) String uploadDate) {
        try {
            boolean statisSwitch = !marketingCommonConfig.getUploadAndTransferDataRealtimeStatisSwitch();
            if (statisSwitch) {
                syncReportService.syncReportProcess(uploadDate);
            }
            return new ApiResult<Boolean>().success(Boolean.TRUE);
        } catch (Exception e) {
            log.warn("手动执行上传数据统计报表任务异常");
            return new ApiResult<Boolean>().fail("手动执行上传数据统计报表任务异常,请稍后再试！");
        }
    }

    @Operation(summary = "修改有效期记录", description = "修改有效期记录")
    @Parameters({@Parameter(name = "ids", value = "ids", required = true, dataType = "List<Long>")
            , @Parameter(name = "validStartDate", value = "生效开始日期", required = true, paramType = "query", dataType = "String")
            , @Parameter(name = "validEndDate", value = "生效结束日期", required = true, paramType = "query", dataType = "String")
    })
    @GetMapping("/updateValidity")
    public ApiResult<Boolean> updateValidity(@RequestParam List<Long> ids
            , @RequestParam String validStartDate
            , @RequestParam String validEndDate) {
        try {
            boolean flag = syncReportService.updateById(ids, validStartDate, validEndDate);
            if (flag) {
                return new ApiResult<Boolean>().success(true, "操作成功！");
            } else {
                return new ApiResult<Boolean>().fail(false, "操作失败！");
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(), ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "统计管理-上传记录-手机号查询列表", description = "客户上传数据统计报表列表")
    @Parameters({@Parameter(name = "cidOrName", value = "客户名称/客户编号", paramType = "query", dataType = "string")
            , @Parameter(name = "appletTimeStart", value = "查询开始时间", paramType = "query", dataType = "string")
            , @Parameter(name = "appletTimeEnd", value = "查询结束时间", paramType = "query", dataType = "string")
            , @Parameter(name = "apiCodes", value = "piCode筛选,支持多选,逗号分隔", paramType = "query", dataType = "string")
            , @Parameter(name = "userTypes", value = "场景筛选,支持多选,逗号分隔(例：S01,S02,促首登)", paramType = "query", dataType = "string")
            , @Parameter(name = "cell", value = "精确手机号（明文、md5、sha256）", paramType = "query", dataType = "string")
            , @Parameter(name = "orderField", value = "排序字段)", paramType = "query", dataType = "string", defaultValue = "applet_date")
            , @Parameter(name = "descField", value = "升序asc/降序desc)", paramType = "query", dataType = "string", defaultValue = "desc")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = JSONObject.class)
            ,@ApiResponse(code = 5001, message = "服务器正忙，请稍后再试", response = JSONObject.class)
            ,@ApiResponse(code = 000000, message = "成功", response = JSONObject.class)
    })
    @PostMapping("/getReportByCell")
    public ApiResult<JSONObject> getReportByCell(@RequestParam(required = false) String cidOrName
            , @RequestParam(required = false) String appletTimeStart
            , @RequestParam(required = false) String appletTimeEnd
            , @RequestParam(required = false) String apiCodes
            , @RequestParam(required = false) String userTypes
            , @RequestParam(required = false) String cell
            , @RequestParam(defaultValue = "applet_date") String orderField
            , @RequestParam(defaultValue = "desc") String descField) {
        if(StringUtils.isBlank(apiCodes) || StringUtils.isBlank(cell)){
            return new ApiResult<JSONObject>().fail(ServiceResultEnum.SUCCESS_1);
        }
        JSONObject cellList = marketingSyncReportService.getReportByCell(cidOrName, appletTimeStart, appletTimeEnd
                , apiCodes, userTypes, cell, orderField, descField);
        if (cellList != null) {
            return new ApiResult<JSONObject>().success(cellList);
        }
        return new ApiResult<JSONObject>().fail(ServiceResultEnum.FAILED);
    }

}
