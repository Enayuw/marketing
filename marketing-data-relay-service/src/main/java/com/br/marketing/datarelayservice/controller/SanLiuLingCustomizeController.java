package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.service.SanLiuLingUploadDataService;
import com.br.marketing.dto.sanliuling.response.SanLiuLingResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName SanLiuLingCustomizeController
 * @Description 360催收Agent
 * @Author kongbx
 * @Date 2025/8/28 14:05
 */
@Tag(name = "360催收Agent", description = "360催收Agent")
@RequestMapping("/marketing/v1/collection")
@RestController
@Slf4j
public class SanLiuLingCustomizeController {

    @Resource
    private SanLiuLingUploadDataService sanLiuLingUploadDataService;

    @Operation(summary = "360催收Agent数据上传接口")
    @PostMapping("/upload")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public SanLiuLingResponseDTO receiveCollectionUploadData(@RequestBody String jsonData, HttpServletRequest request) {
        return sanLiuLingUploadDataService.receiveCollectionUploadData(jsonData, request);
    }

}
