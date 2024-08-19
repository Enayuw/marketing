package com.br.marketing.innerapi.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.marketing.aspect.AuthDataControllerPermission;
import com.br.marketing.client.FastDfsClient;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.service.ReportScoreRuleService;
import com.br.marketing.service.bi.AnalysisReportService;
import com.br.marketing.vo.bi.AxisWrapVO;
import com.br.marketing.vo.bi.param.ReportTaskParam;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 前端页面 跑分模型分布 规则选择并保存任务记录 功能对应接口 技术方案地址： https://c.100credit.cn/pages/viewpage.action?pageId=174496665
 * 
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-14
 */
@Slf4j
@RestController
@RequestMapping("/reportScoreRule")
public class ReportScoreRuleController {

    @Resource
    ReportScoreRuleService reportScoreRuleService;
    @Resource
    private AnalysisReportService analysisReportService;
    @Resource
    private FastDfsClient fastDfsClient;

    @GetMapping("/getTaskScoreProducts")
    public ApiResult<Map> getTaskScoreProducts(@RequestParam(required = true) String ids) {
        return new ApiResult<Map>().success(reportScoreRuleService.getProducts(ids));
    }

    @ApiOperation(value = "获取报告任务列表")
    @GetMapping("/getReportTaskList")
    @AuthDataControllerPermission
    public ApiResult<PageResultReturn> getReportTaskList(@RequestParam(defaultValue = "1") int current, @RequestParam(defaultValue = "10") int size,
        @RequestParam String name, @RequestParam List<String> apiCodes) {
        PageResultReturn listPage = reportScoreRuleService.getReportTaskList(current, size, apiCodes);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    @PostMapping("/addReportTaskScore")
    public ApiResult<Boolean> addReportTaskScore(@RequestBody ReportTaskParam reportTaskParam) {
        try {
            return reportScoreRuleService.addReportTask(reportTaskParam);
        } catch (Exception e) {
            log.warn("添加跑分报表任务异常,入参:{}--", reportTaskParam, e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "生成报告文件并上传至fastdfs")
    @GetMapping("/uploadReportToFastDfs")
    public ApiResult<String> uploadReportToFastDfs(@RequestParam Long taskId) {
        try {
            return new ApiResult<String>().success().setData(analysisReportService.uploadReportToFastDfs(taskId));
        } catch (Exception e) {
            log.warn("生成报告文件并上传至fastdfs异常,入参:{}--", taskId, e);
            return new ApiResult<String>().fail(null, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "获取报告详情")
    @GetMapping("/getReportDetails")
    public ApiResult<List<AxisWrapVO>> getReportDetails(@RequestParam Long taskId) {
        try {
            return new ApiResult<List<AxisWrapVO>>().success(analysisReportService.getReportDetailsByTaskId(taskId));
        } catch (Exception e) {
            log.warn("获取报告详情异常,入参:{}--", taskId, e);
            return new ApiResult<List<AxisWrapVO>>().fail(null, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "下载fastdfs文件")
    @GetMapping("/downloadFile")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String fileName, @RequestParam String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            // 设置下载协议头，防止中文乱码做URLEncoder处理
            String encodeFileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8.toString());
            headers.set("Content-Disposition", "attachment;filename*=UTF-8''" + encodeFileName);
            byte[] bytes = fastDfsClient.downloadFile(url);
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
