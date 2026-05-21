package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.context.TcRelayServerConfigContext;
import com.br.marketing.datarelayservice.service.TcCpaCustomizeService;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.dto.tc.TcResponseDTO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Tag(name = "同程易融cpa代运营", description = "正式链路；验签使用 Speed tcyrServerConfig（经 ThreadLocal 注入）")
@RequestMapping("/marketing/v1/api/cpa")
@RestController
@Slf4j
public class TcCpaCustomizeController {

    @Resource
    private TcCpaCustomizeService tcCpaCustomizeService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Operation(summary = "数据推送")
    @PostMapping("/marketDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO marketDataPush(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        try {
            TcRelayServerConfigContext.setMain(marketingCommonConfig);
            return tcCpaCustomizeService.marketDataPush(tcRequestDTO, request.getHeader("Test-ApiCode"));
        } finally {
            TcRelayServerConfigContext.clear();
        }
    }

    @Operation(summary = "撤销营销")
    @PostMapping("/marketRevoke")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO marketRevoke(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        try {
            TcRelayServerConfigContext.setMain(marketingCommonConfig);
            return tcCpaCustomizeService.marketRevoke(tcRequestDTO, request.getHeader("Test-ApiCode"));
        } finally {
            TcRelayServerConfigContext.clear();
        }
    }

    @Operation(summary = "转化通知")
    @PostMapping("/transformNotify")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO transformNotify(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        try {
            TcRelayServerConfigContext.setMain(marketingCommonConfig);
            return tcCpaCustomizeService.transformNotify(tcRequestDTO, request.getHeader("Test-ApiCode"));
        } finally {
            TcRelayServerConfigContext.clear();
        }
    }

    @Operation(summary = "正负样本推送")
    @PostMapping("/sampleDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO sampleDataPush(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        try {
            TcRelayServerConfigContext.setMain(marketingCommonConfig);
            return tcCpaCustomizeService.sampleDataPush(tcRequestDTO, request.getHeader("Test-ApiCode"));
        } finally {
            TcRelayServerConfigContext.clear();
        }
    }

    @Operation(summary = "撞库失败数据推送")
    @PostMapping("/marketFailDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO marketFailDataPush(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        try {
            TcRelayServerConfigContext.setMain(marketingCommonConfig);
            return tcCpaCustomizeService.marketFailDataPush(tcRequestDTO, request.getHeader("Test-ApiCode"));
        } finally {
            TcRelayServerConfigContext.clear();
        }
    }
}
