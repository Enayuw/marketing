package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.service.SmyUploadDataService;
import com.br.marketing.datarelayservice.service.TcCustomizeService;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.dto.tc.TcResponseDTO;
import com.br.marketing.util.tc.RSAUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(value = "同程易融代运营")
@RequestMapping("/marketing/v1/api")
@RestController
@Slf4j
public class TcCustomizeController {

    @Resource
    private TcCustomizeService tcCustomizeService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String brPrivateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCz0d7j3WGnfMIjvSspB7v2ZcmCWJ8Zqzn1gvMe99bqWOnve7V3fpTZSdZVMg7B8viJDUtrUqDY/ZSWsUojW0N374bzjYlGNZYlWmYqiZfKZZ44ABK6+dky5MDhnaTpYihlhwxiKp8P48awjyMxgqWxv7dWhSEgVFvhDkIQr9XYtQf11EL2s1N6bqkaf1uvGARAb6yDz9vh4torzow+bmfe0HejH1Qyw5C3eza+WEmhFdBFEeH4GhkLwp3wQ6mopQDpyPPXzrNJIo7zaqbaJ+s+LTj2q8ECV8hHndwSHrai0fogkadAMF9tgTD2UwHZAtvK0K8IW1RL0E8kFIamflWVAgMBAAECggEASu+H0i+clX6RLPGVPekCNIFgg1hJHRpU8fIbPOmNf2WEP4+vJNf0UcTKdACDU+HcHskSh+wMKcErHc1OFwPeTunbtD1kWoTUSEau0sU6I1dLowyswYyDLglUM/FNGxETwpOP3ozicm26jDNqOCS4xiUd0wlxr5ZYH6agc3HDTSYXG6M+Q4q8jhHs0oEpr4b4aL0oDNHnEg7mPynS/LEritzr1D06dBXbF6UTlOVB/swnvsidAEHbdzIbmL+sH+pqljnkgk1wtrkqWTDJj4apzmQIuDiRwk8yWtPHiQTxhqk0EKSTsTVie0KaEuW7NrgcaGFhSFlrkCOhx+oEpWkWoQKBgQD40s73g8BR70LcG7N9V9ITh+7IkW6BptKJr8nKCm7+xTyGh7aInwT50PJBVJVmfVub4Bf5wVD6BE1IJ/Ss9WYJPZa2pK0QqwFElGKDzxx19NdBNqsR3W8t6CaM39iokUwaDLC4YUK41IJ1h9uFsHPJg4raX/ye1d/uQijedx1TrQKBgQC5AZKcWC2xU7Gz0GNjupkhfmt+H1+jXPybwM9kz1pScZ5z0bbZ8ZuiS0VC0eI+ILLEMa5UmeSik5ZEJJHLFwzQgQukNBQeU+llRRWqSmXyabkJD3zC85hQCm2kUbLUVzexgvB7CPL1hqQT6ayMItQf9+/2jgrkRrHUalD/hgCGiQKBgFKY0AFT5/SK4vvj6ioyi9bV6csEk9VQBlWUV/zMh9nkqVnTFSG2/9TZqoFLTajO9ikBM5RButqzsN/B+7OqZmus2SnZ8mU1Dt+wDh/JEZ6KXyYTuqfchLqNdLaQ2//g8402JzedeaOXT5MqPRHc6CK9msswz8/+GS6jIaPvkHmlAoGAWfPazivNo6+28l/7Q01CEVf/eeZVQQAATtazwCdVmkpmKZgpGNTxwDpq5a9ZGq4ZXW1ufvIIicfKwz0oqh99+o8UEvXDZm+URsoNW6wq32/qKO6f0cZRI3G+l6ulkLsLeELbHGdggmLBunDelZCFpTmPMkkkIJQC+O3sjiEgdkkCgYBUBxcHzomyZ/oUZNutF5qMBwzM6S7iLITPNNVAUYRHIGPebeUrTkS+kZgvfP9qLFq41kja8KdauPD1OX2FsE/Q7LpjbxsSfIkL5rdqwpICgX/DMEM9T1450BE0QipPLBR8euhCfxrUPNYiANEPrMmJOvHWgzAjBvV8M1ID1CP/VA==";

    @ApiOperation(value = "数据推送")
    @PostMapping("/marketDataPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public TcResponseDTO marketDataPush(@RequestBody TcRequestDTO tcRequestDTO) {
        return tcCustomizeService.marketDataPush(tcRequestDTO);
    }

    @ApiOperation(value = "数据推送")
    @PostMapping("/marketDataPushWithoutSign")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public TcResponseDTO marketDataPushWithoutSign(@RequestBody TcRequestDTO tcRequestDTO) {
        tcRequestDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        Map<String, Object> convert = objectMapper.convertValue(tcRequestDTO, Map.class);
        String signature = RSAUtil.generateContent(convert);
        String sign = RSAUtil.signByPrivateKey(brPrivateKey, signature);
        tcRequestDTO.setSign(sign);
        return tcCustomizeService.marketDataPush(tcRequestDTO);
    }

    @ApiOperation(value = "撤销营销")
    @PostMapping("/marketRevoke")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public TcResponseDTO marketRevoke(@RequestBody TcRequestDTO tcRequestDTO) {
        return tcCustomizeService.marketRevoke(tcRequestDTO);
    }

    @ApiOperation(value = "撤销营销")
    @PostMapping("/marketRevokeWithoutSign")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public TcResponseDTO marketRevokeWithoutSign(@RequestBody TcRequestDTO tcRequestDTO) {
        tcRequestDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        Map<String, Object> convert = objectMapper.convertValue(tcRequestDTO, Map.class);
        String signature = RSAUtil.generateContent(convert);
        String sign = RSAUtil.signByPrivateKey(brPrivateKey, signature);
        tcRequestDTO.setSign(sign);
        return tcCustomizeService.marketRevoke(tcRequestDTO);
    }

    @ApiOperation(value = "转化通知")
    @PostMapping("/transformNotify")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public TcResponseDTO transformNotify(@RequestBody TcRequestDTO tcRequestDTO) {
        return tcCustomizeService.transformNotify(tcRequestDTO);
    }

    @ApiOperation(value = "转化通知")
    @PostMapping("/transformNotifyWithoutSign")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public TcResponseDTO transformNotifyWithoutSign(@RequestBody TcRequestDTO tcRequestDTO) {
        tcRequestDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        Map<String, Object> convert = objectMapper.convertValue(tcRequestDTO, Map.class);
        String signature = RSAUtil.generateContent(convert);
        String sign = RSAUtil.signByPrivateKey(brPrivateKey, signature);
        tcRequestDTO.setSign(sign);
        return tcCustomizeService.transformNotify(tcRequestDTO);
    }
}
