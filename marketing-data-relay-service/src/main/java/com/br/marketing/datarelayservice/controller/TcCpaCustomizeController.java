package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.service.TcCpaCustomizeService;
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

@Api(value = "同程易融cpa代运营")
@RequestMapping("/marketing/v1/api/cpa")
@RestController
@Slf4j
public class TcCpaCustomizeController {

    @Resource
    private TcCpaCustomizeService tcCpaCustomizeService;

    @ApiOperation(value = "数据推送")
    @PostMapping("/marketDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO marketDataPush(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        return tcCpaCustomizeService.marketDataPush(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }

    @ApiOperation(value = "撤销营销")
    @PostMapping("/marketRevoke")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO marketRevoke(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        return tcCpaCustomizeService.marketRevoke(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }

    @ApiOperation(value = "转化通知")
    @PostMapping("/transformNotify")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO transformNotify(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        return tcCpaCustomizeService.transformNotify(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }

    @ApiOperation(value = "正负样本推送")
    @PostMapping("/sampleDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO sampleDataPush(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        return tcCpaCustomizeService.sampleDataPush(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }

    @ApiOperation(value = "撞库失败数据推送")
    @PostMapping("/marketFailDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO marketFailDataPush(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        return tcCpaCustomizeService.marketFailDataPush(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }
}
