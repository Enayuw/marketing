package com.br.marketing.innerapi.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import com.br.marketing.aspect.LogRecordAnnotation;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.InterfaceOperationsEnum;
import com.br.marketing.vo.bi.IntervalTemplateVO;
import com.br.marketing.vo.bi.ReportTaskVO;
import com.br.marketing.vo.bi.param.BiReportStatisticTransferParam;
import com.br.marketing.vo.bi.param.BiReportTaskParam;
import com.br.marketing.vo.bi.param.IntervalTemplateParam;
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
import com.br.marketing.dto.report.RefreshReportRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
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

    private static final Integer CODE_1 = Integer.valueOf(1);

    @GetMapping("/getTaskScoreProducts")
    public ApiResult<Map> getTaskScoreProducts(@RequestParam(required = true) String ids, @RequestParam(defaultValue = "all")String fieldType) {
        return new ApiResult<Map>().success(reportScoreRuleService.getProducts(ids, fieldType));
    }

    @Operation(summary = "获取报告任务列表")
    @GetMapping("/getReportTaskList")
    @AuthDataControllerPermission
    public ApiResult<PageResultReturn> getReportTaskList(@RequestParam(defaultValue = "1") int current, @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String name, @RequestParam(required = false) List<String> apiCodes) {
        PageResultReturn listPage = reportScoreRuleService.getReportTaskList(current, size, name,apiCodes);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    @PostMapping("/addReportTaskScore")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.BI_ADD_DISTRIBUTED_REPORT, extendInfo = "新建跑分分布报表{#reportTaskParam.reportName}")
    public ApiResult<Boolean> addReportTaskScore(@RequestBody ReportTaskParam reportTaskParam) {
        try {
            return reportScoreRuleService.addReportTask(reportTaskParam);
        } catch (KnowException ke) {
            return new ApiResult<Boolean>().fail(false, ke.getMessage());
        } catch (Exception e) {
            log.warn("添加跑分报表任务异常,入参:{}--", reportTaskParam, e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "生成报告文件并上传至fastdfs")
    @GetMapping("/uploadReportToFastDfs")
    public ApiResult<String> uploadReportToFastDfs(@RequestParam Long taskId) {
        try {
            return new ApiResult<String>().success().setData(analysisReportService.uploadReportToFastDfs(taskId));
        } catch (Exception e) {
            log.warn("生成报告文件并上传至fastdfs异常,入参:{}--", taskId, e);
            return new ApiResult<String>().fail(null, ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "获取报告详情")
    @GetMapping("/getReportDetails")
    public ApiResult<List<AxisWrapVO>> getReportDetails(@RequestParam Long taskId) {
        try {
            return new ApiResult<List<AxisWrapVO>>().success(analysisReportService.getReportDetailsByTaskId(taskId));
        } catch (Exception e) {
            log.warn("获取报告详情异常,入参:{}--", taskId, e);
            return new ApiResult<List<AxisWrapVO>>().fail(null, ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "下载fastdfs文件")
    @GetMapping("/downloadFile")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.BI_DOWNLOAD_DISTRIBUTED_REPORT, extendInfo = "下载跑分分布报表：{#fileName}，下载文件路径：{#url}")
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

    @Operation(summary = "获取Bi报表列表")
    @PostMapping("/getBiReportTaskList")
    @AuthDataControllerPermission
    public ApiResult<PageResultReturn> getBiReportTaskList(@RequestBody(required=false) BiReportTaskParam reportTaskParam) {
        PageResultReturn listPage = reportScoreRuleService.getBiReportTaskList(reportTaskParam);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    @Operation(summary = "获取跑分文件对应评分分布列表")
    @GetMapping("/getReportTaskListForScore")
    @AuthDataControllerPermission
    public ApiResult<List<ReportTaskVO>> getReportTaskListForScore(@RequestParam(required = false) String name, @RequestParam String ids) {
        List<ReportTaskVO> list = reportScoreRuleService.getReportTaskListForScore(name, ids);
        return new ApiResult<List<ReportTaskVO>>().success(list);
    }

    @Operation(summary = "更新报表统计记录")
    @PostMapping("/updateReportStatisticsRecords")
    public ApiResult<Boolean> updateReportStatisticsRecords(@RequestBody(required = false) BiReportStatisticTransferParam param) {
        try {
            return new ApiResult<Boolean>().success(reportScoreRuleService.updateReportRecords(param));
        } catch (Exception e) {
            log.error("更新报表统计记录异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "重命名报表名称")
    @GetMapping("/updateReportName")
    @AuthDataControllerPermission
    public ApiResult<Boolean> updateReportName(@RequestParam Long id, @RequestParam String reportName) {
        return reportScoreRuleService.updateReportName(id, reportName);
    }

    @Operation(summary = "报表删除")
    @GetMapping("/deleteReport")
    @AuthDataControllerPermission
    public ApiResult<Boolean> deleteReport(@RequestParam Long id) {
        return reportScoreRuleService.deleteReport(id);
    }

    @Operation(summary = "刷新报表数据")
    @PostMapping("/refreshCustomIntervalReport")
    @AuthDataControllerPermission
    public ApiResult<Boolean> refreshCustomIntervalReport(@RequestBody RefreshReportRequestDTO requestDTO) {
        return new ApiResult().fromResult(reportScoreRuleService.refreshCustomIntervalReport(requestDTO), CODE_1);
    }

    @Operation(summary = "保存评分分布模板")
    @PostMapping("/saveIntervalTemplate")
    @AuthDataControllerPermission
    public ApiResult<Boolean> saveIntervalTemplate(@RequestBody RefreshReportRequestDTO requestDTO) {
        MarketingUserDetail user = ThreadContextInfo.getUser();
        return new ApiResult().fromResult(reportScoreRuleService.saveIntervalTemplate(requestDTO,user), CODE_1);
    }

    @Operation(summary = "评分分布查询规则模板")
    @GetMapping("/getIntervalTemplate")
    @AuthDataControllerPermission
    public ApiResult<List<IntervalTemplateVO>> getIntervalTemplate(@RequestParam String apiCode) {
        return new ApiResult().fromResult(reportScoreRuleService.getIntervalTemplate(apiCode), CODE_1);
    }

    @Operation(summary = "查询画像分布模型")
    @GetMapping("/getImageDistribution")
    public ApiResult<String> getImageDistribution() {
        return new ApiResult().fromResult(reportScoreRuleService.getImageDistribution(), CODE_1);
    }

}
