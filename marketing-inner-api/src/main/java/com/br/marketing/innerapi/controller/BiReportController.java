package com.br.marketing.innerapi.controller;

import com.br.marketing.aspect.LogRecordAnnotation;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.enums.InterfaceOperationsEnum;
import com.br.marketing.service.bi.BiReportService;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * BI报表相关接口
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@RestController
@RequestMapping(value = "/bi/report")
@Api(value = "BI报表相关接口", tags = "BI报表相关接口")
@Slf4j
public class BiReportController {

    @Resource
    private BiReportService biReportService;

    @ApiOperation(value = "查看BI报表")
    @PostMapping(value = "/getBiReport")
    public ApiResult<BiReportVO> getBiReport(@RequestBody BiReportParam param) {
        log.warn("查看BI报表,请求参数{}", param);
        BiReportVO biReportVO = biReportService.getBiReport(param);
        if (biReportVO != null) {
            return new ApiResult<BiReportVO>().success(biReportVO);
        }
        return new ApiResult<BiReportVO>().fail(ServiceResultEnum.FAILED);
    }

    @ApiOperation(value = "下载BI报表")
    @PostMapping("/downloadReport")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.BI_DOWNLOAD_REPORT, extendInfo = "下载BI报表类型：{#param.reportTypeName}，BI报表名称：{#param.reportName}")
    public void downloadReport(@RequestBody BiReportDownLoadParam param, HttpServletRequest request, HttpServletResponse response) throws Exception {
        biReportService.downloadReport(param, request, response);
    }
}
