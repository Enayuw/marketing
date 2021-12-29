package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.service.MarketingSyncReportService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


    @GetMapping("/getReportList")
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

    @GetMapping("/getReportListTotal")
    @ApiOperation(value = "客户上传数据统计报表总计", notes = "客户上传数据统计报表列表总计", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "cidOrName", value = "客户名称/客户编号",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "appletTimeStart", value = "上传日期开始",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "appletTimeEnd",value = "上传日期截至", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "apiCodes", value = "apiCode筛选,支持多选,逗号分隔", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "userTypes", value = "场景筛选,支持多选,逗号分隔(例：S01,S02,促首登)", paramType = "query", dataType = "string")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingSyncReport.class)})
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

}
