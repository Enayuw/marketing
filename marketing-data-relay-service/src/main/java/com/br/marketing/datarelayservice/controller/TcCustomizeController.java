package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.service.TcCustomizeService;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.dto.tc.TcResponseDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(value = "同程易融代运营")
@RequestMapping("/marketing/v1/api")
@RestController
@Slf4j
public class TcCustomizeController {

    @Resource
    private TcCustomizeService tcCustomizeService;

    @ApiOperation(value = "数据推送")
    @PostMapping("/marketDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO marketDataPush(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        return tcCustomizeService.marketDataPush(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }

    @ApiOperation(value = "撤销营销")
    @PostMapping("/marketRevoke")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO marketRevoke(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        return tcCustomizeService.marketRevoke(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }

    @ApiOperation(value = "转化通知")
    @PostMapping("/transformNotify")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO transformNotify(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        log.warn("同程易融转化通知数据上传接口被调用");
        return tcCustomizeService.transformNotify(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }

    @ApiOperation(value = "正负样本推送")
    @PostMapping("/sampleDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO sampleDataPush(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        return tcCustomizeService.sampleDataPush(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }
}
