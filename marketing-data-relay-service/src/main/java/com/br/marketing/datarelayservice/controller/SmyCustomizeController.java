package com.br.marketing.datarelayservice.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.dto.smy.response.SmyResponseDTO;
import com.br.marketing.datarelayservice.service.SmyTransferDataService;
import com.br.marketing.datarelayservice.service.SmyUploadDataService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Api(value = "萨摩耶定制传输数据接口")
@RequestMapping("/marketing/v1/smy")
@RestController
@Slf4j
public class SmyCustomizeController {

    @Resource
    private SmyUploadDataService smyUploadDataService;

    @Resource
    private SmyTransferDataService smyTransferDataService;


    @ApiOperation(value = "萨摩耶代运营数据上传")
    @PostMapping("/upload")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public SmyResponseDTO receiveSmyUploadData(@RequestBody String jsonData) {

        return smyUploadDataService.receiveSmyUploadData(jsonData);
    }

    @ApiOperation(value = "萨摩耶代运营数据上传")
    @PostMapping("/transfer")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public SmyResponseDTO receiveSmyTransferData(@RequestBody String jsonData) {
        return smyTransferDataService.receiveSmyTransferData(jsonData);
    }
}
