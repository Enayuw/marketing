package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.service.TransferFileTaskService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 转化文件任务表
 *
 * @author songjuanjuan
 * @dateTime 2022/05/26 11:12
 */
@RestController
@RequestMapping(value = "/transferFile")
@Api(value = "转化文件任务表", tags = "转化文件任务表", produces = "application/json", consumes = "application/json", protocols = "http")
public class TransferFileTaskController {

    private static final Logger log = LoggerFactory.getLogger(TransferFileTaskController.class);

    @Resource
    private TransferFileTaskService transferFileTaskService;


    @GetMapping("/getTransferFileList")
    @ApiOperation(value = "数据提取列表", notes = "数据提取列表", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "serach", value = "搜索输入",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "startDateStart", value = "执行日期开始",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "startDateEnd",value = "执行日期截至", paramType = "query", dataType = "string")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingSyncReport.class)})
    public ApiResult<PageResultReturn> getReportList(@RequestParam(defaultValue = "1") int current
                                                        , @RequestParam(defaultValue = "10") int size
                                                        , @RequestParam(required = false) String serach
                                                        , @RequestParam(required = false) String startDateStart
                                                        , @RequestParam(required = false) String startDateEnd) {
        PageResultReturn listPage = transferFileTaskService.getTransferFileList(current, size, serach,startDateStart,startDateEnd);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }



}
