package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.MarketingSyncReportService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 客户上传数据统计报表
 *
 * @author songjuanjuan
 * @dateTime 2021/11/18 15:12
 */
@RestController
@RequestMapping(value = "/rule/report")
@Api(value = "客户上传数据统计报表", tags = "客户上传数据统计报表", produces = "application/json", consumes = "application/json", protocols = "http")
public class SyncReportController {

    private static final Logger log = LoggerFactory.getLogger(SyncReportController.class);

    @Resource
    private MarketingSyncReportService marketingSyncReportService;

    @Resource
    private MarketingSyncReportService syncReportService;

    @PostMapping("/getReportList")
    @ApiOperation(value = "客户上传数据统计报表列表", notes = "客户上传数据统计报表列表", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "cidOrName", value = "客户名称/客户编号",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "appletTimeStart", value = "上传日期开始",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "appletTimeEnd",value = "上传日期截至", paramType = "query", dataType = "string")
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
        PageResultReturn listPage = marketingSyncReportService.getReportList(current, size, cidOrName,appletTimeStart,appletTimeEnd,apiCodes,userTypes);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    @PostMapping("/getReportListTotal")
    @ApiOperation(value = "客户上传数据统计报表总计", notes = "客户上传数据统计报表列表总计", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "cidOrName", value = "客户名称/客户编号",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "appletTimeStart", value = "上传日期开始",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "appletTimeEnd",value = "上传日期截至", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "apiCodes", value = "apiCode筛选,支持多选,逗号分隔", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "userTypes", value = "场景筛选,支持多选,逗号分隔(例：S01,S02,促首登)", paramType = "query", dataType = "string")
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
    @ApiOperation(value = "手动执行上传数据统计报表任务", notes = "手动执行上传数据统计报表任务", httpMethod = "GET")
    @ApiImplicitParam(name = "uploadDate", value = "当日日期(yyyy-MM-dd)",paramType = "query", dataType = "string")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingSyncReport.class)})
    public ApiResult<Boolean> triggerTaskUploadSyncReportJob(@RequestParam(required = false) String uploadDate) {
        try {
            syncReportService.syncReportProcess(uploadDate);
            return new ApiResult<Boolean>().success(Boolean.TRUE);
        }catch (Exception e){
            log.warn("手动执行上传数据统计报表任务异常");
            return new ApiResult<Boolean>().fail("手动执行上传数据统计报表任务异常,请稍后再试！");
        }
    }

    @ApiOperation(value = "修改有效期记录", notes = "修改有效期记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id", required = true, dataType = "Long")
            , @ApiImplicitParam(name = "validStartDate", value = "生效开始日期", required = true, paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "validEndDate", value = "生效结束日期", required = true, paramType = "query", dataType = "String")
    })
    @GetMapping("/updateValidity")
    public ApiResult<Boolean> updateValidity(@RequestParam Long id
            , @RequestParam String validStartDate
            , @RequestParam String validEndDate) {
        try {
            boolean flag = syncReportService.updateById(id, validStartDate, validEndDate);
            if (flag) {
                return new ApiResult<Boolean>().success(true, "操作成功！");
            } else {
                return new ApiResult<Boolean>().success(false, "操作失败！");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

}
