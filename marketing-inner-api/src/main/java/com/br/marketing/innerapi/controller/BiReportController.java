package com.br.marketing.innerapi.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.br.marketing.aspect.AuthDataControllerPermission;
import com.br.marketing.aspect.LogRecordAnnotation;
import com.br.marketing.client.FastDfsClient;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.enums.InterfaceOperationsEnum;
import com.br.marketing.service.bi.BiReportService;
import com.br.marketing.vo.bi.BiReportConfigDictVO;
import com.br.marketing.vo.bi.BiReportTimeRangeVO;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.param.BiReportConfigDictParam;
import com.br.marketing.vo.bi.param.BiReportConfigParam;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * BI报表相关接口
 * @author senyang.zheng
 * @date 2024/08/28
 */
@RestController
@RequestMapping(value = "/bi")
@Tag(name = "BI报表相关接口", description = "BI报表相关接口")
@Slf4j
public class BiReportController {

    @Resource
    private BiReportService biReportService;
    @Resource
    private FastDfsClient fastDfsClient;

    @Operation(summary = "查看BI报表")
    @PostMapping(value = "/report/getBiReport")
    @AuthDataControllerPermission
    public ApiResult<List<BiReportVO>> getBiReport(@RequestBody BiReportParam param) {
        log.warn("查看BI报表,请求参数{}", param);
        List<BiReportVO> biReportVOList = biReportService.getBiReport(param);
        if (CollectionUtil.isNotEmpty(biReportVOList)) {
            return new ApiResult<List<BiReportVO>>().success(biReportVOList);
        }
        return new ApiResult<List<BiReportVO>>().fail(ServiceResultEnum.FAILED);
    }

    @Operation(summary = "下载BI报表")
    @PostMapping("/report/downloadReport")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.BI_DOWNLOAD_REPORT, extendInfo = "下载BI报表类型：{#params[0].reportTypeName}，BI报表名称：{#params[0]" +
            ".reportName}")
    public String downloadReport(@RequestBody List<BiReportDownLoadParam> params, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return biReportService.downloadReport(params, request, response);
    }


    @Operation(summary = "下载fastdfs文件")
    @GetMapping("/downloadFile")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String url) {
        try {
            String[] segments = url.split("/");
            String fileName = segments.length > 0 ? segments[segments.length - 1] : "default.txt";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
            headers.set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
            byte[] bytes = fastDfsClient.downloadFile(url);
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "查看BI报表配置字典")
    @PostMapping(value = "/report/getBiReportConfigDict")
    public ApiResult<List<BiReportConfigDictVO>> getBiReportConfigDict(@RequestBody BiReportConfigDictParam param) {
        log.warn("查看BI报表配置字典,请求参数{}", param);
        List<BiReportConfigDictVO> biReportConfigDictVO = biReportService.getBiReportConfigDict(param);
        return new ApiResult<List<BiReportConfigDictVO>>().success(biReportConfigDictVO);
    }

    @Operation(summary = "新增修改BI报表配置字典")
    @PostMapping(value = "/report/saveBiReportConfigDict")
    public ApiResult<Boolean> saveBiReportConfigDict(@RequestBody BiReportConfigDictParam param) {
        log.warn("新增修改BI报表配置字典,请求参数{}", param);
        return biReportService.saveBiReportConfigDict(param);
    }

    @Operation(summary = "获取报表分组维度")
    @PostMapping("/config/getReportGroupList")
    public ApiResult<List<String>> getReportGroupList(@RequestBody BiReportConfigParam param) {
        try {
            return new ApiResult<List<String>>().success().setData(biReportService.getReportGroupList(param));
        } catch (Exception e) {
            log.warn("获取报表分组维度,入参:{}--", param, e);
            return new ApiResult<List<String>>().fail(null, ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "获取数据时间范围")
    @PostMapping("/config/getReportTimeRange")
    public ApiResult<BiReportTimeRangeVO> getReportTimeRange(@RequestBody BiReportConfigParam param) {
        try {
            return new ApiResult<BiReportTimeRangeVO>().success().setData(biReportService.getReportTimeRange(param));
        } catch (Exception e) {
            log.warn("获取报表分组维度,入参:{}--", param, e);
            return new ApiResult<BiReportTimeRangeVO>().fail(null, ServiceResultEnum.FAILED);
        }
    }
}
