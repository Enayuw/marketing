package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.datarelayservice.client.QiFuAiReqDTO;
import com.br.marketing.datarelayservice.service.QiFuCustomizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.Resource;

/**
 * @ClassName QiFuCustomizeController
 * @Description 奇富360促动支相关
 * @Author kongbx
 * @Date 2025/6/9 14:03
 */
@Tag(name = "QiFuCustomizeController", description = "QiFuCustomizeController")
@RequestMapping("/marketing/v1/actuation")
@RestController
@Slf4j
public class QiFuCustomizeController {

    @Resource
    private QiFuCustomizeService qiFuCustomizeService;

    //《暂时不启动》
    //@Operation(summary = "促动分析效果统计数据报表")
    //@PostMapping("/analysisStatistics")
    //@PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    //public ApiResult analysisStatistics(@RequestBody QiFuAiReqDTO requestBody) {
    //    return qiFuCustomizeService.handle(requestBody);
    //}

}
