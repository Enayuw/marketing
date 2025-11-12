package com.br.marketing.api.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.api.service.QiFuDataService;
import com.br.marketing.aspect.ReqLogAnnotation;
import com.br.marketing.common.annoation.SaveLog;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.service.ValidityPeriodDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 营销数据接入接口
 */
@Tag(name = "marketingUser", description = "营销用户相关接口")
@RequestMapping("/marketingUser")
@RestController
public class MarketingValidityPeriod {
    private static final Logger log = LoggerFactory.getLogger(MarketingValidityPeriod.class);

    @Resource
    private ValidityPeriodDataService validityPeriodDataService;

    @Resource
    private QiFuDataService qiFuDataService;




    /**
     *  智能营销数据有效期更改接口
     * @param apiCode apiCode
     * @param jsonData jsonData
     * @return ApiNoDataResult
     */
    @Operation(summary = "智能营销数据有效期更改接口")
    @PostMapping("/changeValidityPeriod")
    @ReqLogAnnotation()
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ApiNoDataResult changeValidityPeriod(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        log.warn("有效期变更接口入参：{},{}",apiCode,jsonData);
        ApiNoDataResult apiNoDataResult = validityPeriodDataService.marketingValidityPeriod(apiCode, jsonData);
        return apiNoDataResult;
    }


    /**
     * 360策略效果数据报表-接口
     * @param apiCode apiCode
     * @param jsonData jsonData
     * @return ApiNoDataResult
     */
    @Operation(summary = "360策略效果数据报表接口")
    @PostMapping("/strategyReport")
    @ReqLogAnnotation()
    public ApiNoDataResult strategyReport(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        log.warn("360策略效果数据报表接口入参：{},{}",apiCode,jsonData);
        ApiNoDataResult apiNoDataResult = qiFuDataService.strategyReportData(apiCode, jsonData);
        return apiNoDataResult;
    }


    /**
     * 促动分析效果统计数据报表-接口
     * @param apiCode apiCode
     * @param jsonData jsonData
     * @return ApiNoDataResult
     */
    @Operation(summary = "促动分析效果统计数据报表接口")
    @PostMapping("/analysisstatistics")
    @ReqLogAnnotation()
    public ApiNoDataResult analysisstatistics(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        log.warn("促动分析效果统计数据报表接口入参：{},{}",apiCode,jsonData);
        ApiNoDataResult apiNoDataResult = qiFuDataService.analysisStatistics(apiCode, jsonData);
        return apiNoDataResult;
    }

    @Operation(summary = "奇富促完件效果报表新接口（营销）")
    @PostMapping("/qiFuCWJEffectReport")
    @ReqLogAnnotation()
    @SaveLog
    public ApiNoDataResult effectReport(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        log.warn("奇富促完件效果报表新接口（营销）接口入参：{},{}",apiCode,jsonData);
        ApiNoDataResult apiNoDataResult = qiFuDataService.effectReport(apiCode, jsonData);
        return apiNoDataResult;
    }
}
