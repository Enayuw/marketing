package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.service.SmyTransferDataService;
import com.br.marketing.datarelayservice.service.SmyUploadDataService;
import com.br.marketing.dto.smy.request.SmyTransferRequestDTO;
import com.br.marketing.dto.smy.request.SmyUploadRequestDTO;
import com.br.marketing.dto.smy.response.SmyResponseDTO;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(value = "萨摩耶定制传输数据接口")
@RequestMapping("/marketing/v1/smy")
@RestController
@Slf4j
public class SmyCustomizeController {

    @Resource
    private SmyUploadDataService smyUploadDataService;

    @Resource
    private SmyTransferDataService smyTransferDataService;


    @Operation(value = "萨摩耶代运营数据上传接口")
    @PostMapping("/upload")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public SmyResponseDTO receiveSmyUploadData(@RequestBody String jsonData, HttpServletRequest request) {
        return smyUploadDataService.receiveSmyUploadData(jsonData, request);
    }

    @Operation(value = "萨摩耶回传数据上传接口")
    @PostMapping("/transfer")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public SmyResponseDTO receiveSmyTransferData(@RequestBody String jsonData, HttpServletRequest request) {
        return smyTransferDataService.receiveSmyTransferData(jsonData, request);
    }
}
