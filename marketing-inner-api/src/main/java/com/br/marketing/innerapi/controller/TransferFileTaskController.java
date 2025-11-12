package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.TransferFileTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 转化文件任务表
 *
 * @author songjuanjuan
 * @dateTime 2022/05/26 11:12
 */
@RestController
@RequestMapping(value = "/transferFile")
@Tag(name = "转化文件任务表", description = "转化文件任务相关接口")
public class TransferFileTaskController {

    private static final Logger log = LoggerFactory.getLogger(TransferFileTaskController.class);

    @Resource
    private TransferFileTaskService transferFileTaskService;


    @GetMapping("/getTransferFileList")
    @Operation(
            summary = "数据提取列表",
            description = "分页查询转化文件任务列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功",
                            content = @Content(schema = @Schema(implementation = PageResultReturn.class))),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误",
                            content = @Content(schema = @Schema(implementation = MarketingSyncReport.class)))
            }
    )
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getReportList(
            @Parameter(description = "页号", example = "1") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索输入") @RequestParam(required = false) String serach,
            @Parameter(description = "执行日期开始") @RequestParam(required = false) String startDateStart,
            @Parameter(description = "执行日期截至") @RequestParam(required = false) String startDateEnd
    ) {
        PageResultReturn listPage = transferFileTaskService.getTransferFileList(current, size, serach, startDateStart, startDateEnd);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    @PostMapping("/reStartTransfer")
    @Operation(
            summary = "重新执行转化提取",
            description = "根据ID重新执行转化提取任务",
            responses = {
                    @ApiResponse(responseCode = "200", description = "执行成功"),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误",
                            content = @Content(schema = @Schema(implementation = MarketingSyncReport.class)))
            }
    )
    public ApiResult<PageResultReturn> reStartTransfer(
            @Parameter(description = "转化数据id", required = true, example = "1001")
            @RequestParam int id
    ) {
        return transferFileTaskService.reStartTransfer(id);
    }
}