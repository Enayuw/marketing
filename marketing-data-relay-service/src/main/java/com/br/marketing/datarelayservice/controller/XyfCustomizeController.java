package com.br.marketing.datarelayservice.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.datarelayservice.service.XyfCustomizeService;
import com.br.marketing.dto.xyf.XyfEncryptionDTO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.xyf.AESUtils;
import com.br.marketing.util.xyf.RSAUtils;
import com.br.marketing.util.xyf.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 信用飞定制接口 请求&响应 都需加密加签
 * https://c.100credit.cn/pages/viewpage.action?pageId=240290503
 */
@Tag(name = "信用飞催收", description = "信用飞催收")
@RequestMapping("/marketing/ai")
@RestController
@Slf4j
public class XyfCustomizeController {

    @Resource
    private XyfCustomizeService xyfCustomizeService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Operation(summary = "外呼批量提交接口")
    @PostMapping("/batchSubmit")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public XyfEncryptionDTO batchSubmit(@RequestBody XyfEncryptionDTO requestDTO, HttpServletRequest request) {
        return xyfCustomizeService.batchSubmit(requestDTO, request.getHeader("Test-ApiCode"));
    }

    /**
     * 测试环境专用：先按客户方式用 xyfSimCusEncryptionConfig（brPublicKey、xyfPrivateKey）加密加签，
     * 再调用 batchSubmit。请求体为明文业务 JSON（与 XyfSubmitRecord 字段一致）。
     */
    @Operation(summary = "测试环境-模拟客户加密后调用批量提交")
    @PostMapping("/batchSubmitSim")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public XyfEncryptionDTO batchSubmitSim(@RequestBody JSONObject body, HttpServletRequest request) {
        JSONObject config = marketingCommonConfig.getXyfSimCusEncryptionConfig();
        if (config == null) {
            log.warn("batchSubmitSim: xyfSimCusEncryptionConfig is null");
            return null;
        }
        String brPublicKey = config.getString("brPublicKey");
        String rawPrivate = config.getString("xyfPrivateKey");
        String xyfPrivateKey = rawPrivate != null ? rawPrivate.replace("*", "=") : null;
        if (StringUtils.isBlank(brPublicKey) || StringUtils.isBlank(xyfPrivateKey)) {
            log.warn("batchSubmitSim: brPublicKey or xyfPrivateKey missing in xyfSimCusEncryptionConfig");
            return null;
        }
        String bodyStr = Utils.toUnicode(JSON.toJSONString(body));
        String sign = RSAUtils.signByPrivateKey(bodyStr, xyfPrivateKey);
        String aesKey = AESUtils.generateAESKey();
        String encryptData = AESUtils.encrypt(bodyStr, aesKey, false);
        String encryptAesKey = RSAUtils.encryptByPublicKey(aesKey, brPublicKey);
        XyfEncryptionDTO dto = new XyfEncryptionDTO(encryptAesKey, encryptData, sign);
        return xyfCustomizeService.batchSubmit(dto, request.getHeader("Test-ApiCode"));
    }
}
