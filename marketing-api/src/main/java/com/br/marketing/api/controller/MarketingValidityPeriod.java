package com.br.marketing.api.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.service.ValidityPeriodDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(value = "marketingUser")
@RequestMapping("/marketingUser")
@RestController
public class MarketingValidityPeriod {
    private static final Logger log = LoggerFactory.getLogger(MarketingValidityPeriod.class);

    @Resource
    private ValidityPeriodDataService validityPeriodDataService;



    /**
     * 智能营销数据有效期更改接口
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @ApiOperation(value = "智能营销数据有效期更改接口")
    @PostMapping("/changeValidityPeriod")
    @LogAnnotation
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ApiNoDataResult changeValidityPeriod(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        log.warn("有效期变更接口入参：{},{}",apiCode,jsonData);
        return validityPeriodDataService.marketingValidityPeriod(apiCode,jsonData);
    }
}
