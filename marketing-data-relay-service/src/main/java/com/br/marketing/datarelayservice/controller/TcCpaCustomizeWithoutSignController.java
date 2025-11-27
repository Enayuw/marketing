package com.br.marketing.datarelayservice.controller;

import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.service.TcCpaCustomizeService;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.dto.tc.TcResponseDTO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.tc.RSAUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Tag(name = "同程易融cpa代运营测试", description = "同程易融cpa代运营测试")
@RequestMapping("/marketing/v1/api/cpa/withoutSign")
@RestController
@Slf4j
public class TcCpaCustomizeWithoutSignController {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcCpaCustomizeService tcCpaCustomizeService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "测试数据推送")
    @PostMapping("/marketDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO marketDataPushWithoutSign(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        tcRequestDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        Map<String, Object> convert = objectMapper.convertValue(tcRequestDTO, Map.class);
        String signature = RSAUtil.generateContent(convert);
        JSONObject tcyrServerConfig = marketingCommonConfig.getTcyrServerConfig();
        //同程私钥加签
        String tcPrivateKey = tcyrServerConfig.getString("tcPrivateKey").replace("*", "=");
        //百融私钥加签
        String sign = RSAUtil.signByPrivateKey(tcPrivateKey, signature);
        tcRequestDTO.setSign(sign);
        return tcCpaCustomizeService.marketDataPush(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }

    @Operation(summary = "测试撤销营销")
    @PostMapping("/marketRevoke")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO marketRevokeWithoutSign(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        tcRequestDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        Map<String, Object> convert = objectMapper.convertValue(tcRequestDTO, Map.class);
        String signature = RSAUtil.generateContent(convert);
        JSONObject tcyrServerConfig = marketingCommonConfig.getTcyrServerConfig();
        //同程私钥加签
        String tcPrivateKey = tcyrServerConfig.getString("tcPrivateKey").replace("*", "=");
        String sign = RSAUtil.signByPrivateKey(tcPrivateKey, signature);
        tcRequestDTO.setSign(sign);
        return tcCpaCustomizeService.marketRevoke(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }

    @Operation(summary = "测试转化通知")
    @PostMapping("/transformNotify")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO transformNotifyWithoutSign(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        tcRequestDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        Map<String, Object> convert = objectMapper.convertValue(tcRequestDTO, Map.class);
        String signature = RSAUtil.generateContent(convert);
        JSONObject tcyrServerConfig = marketingCommonConfig.getTcyrServerConfig();
        //同程私钥加签
        String tcPrivateKey = tcyrServerConfig.getString("tcPrivateKey").replace("*", "=");
        String sign = RSAUtil.signByPrivateKey(tcPrivateKey, signature);
        tcRequestDTO.setSign(sign);
        return tcCpaCustomizeService.transformNotify(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }

    @Operation(summary = "测试正负样本推送")
    @PostMapping("/sampleDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO sampleDataPushWithoutSign(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        tcRequestDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        Map<String, Object> convert = objectMapper.convertValue(tcRequestDTO, Map.class);
        String signature = RSAUtil.generateContent(convert);
        JSONObject tcyrServerConfig = marketingCommonConfig.getTcyrServerConfig();
        //同程私钥加签
        String tcPrivateKey = tcyrServerConfig.getString("tcPrivateKey").replace("*", "=");
        String sign = RSAUtil.signByPrivateKey(tcPrivateKey, signature);
        tcRequestDTO.setSign(sign);
        return tcCpaCustomizeService.sampleDataPush(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }

    @Operation(summary = "撞库失败数据推送")
    @PostMapping("/marketFailDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public TcResponseDTO marketFailDataPush(@RequestBody TcRequestDTO tcRequestDTO, HttpServletRequest request) {
        tcRequestDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        Map<String, Object> convert = objectMapper.convertValue(tcRequestDTO, Map.class);
        String signature = RSAUtil.generateContent(convert);
        JSONObject tcyrServerConfig = marketingCommonConfig.getTcyrServerConfig();
        //同程私钥加签
        String tcPrivateKey = tcyrServerConfig.getString("tcPrivateKey").replace("*", "=");
        String sign = RSAUtil.signByPrivateKey(tcPrivateKey, signature);
        tcRequestDTO.setSign(sign);
        return tcCpaCustomizeService.marketFailDataPush(tcRequestDTO, request.getHeader("Test-ApiCode"));
    }
}
