package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.client.qifu.enums.CodeEnum;
import com.br.marketing.client.qifu.enums.FlagEnum;
import com.br.marketing.datarelayservice.client.QiFuAiReqDTO;
import com.br.marketing.datarelayservice.client.QiFuAiResDTO;
import com.br.marketing.datarelayservice.service.QiFuAiUploadDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description UploadDataController
 * @Author hong.chen
 * @CreateTime 2024/10/25
 */
@Api(value = "UploadDataController")
@RequestMapping("/marketing/v1")
@RestController
@Slf4j
public class UploadDataController {
    @Resource
    private QiFuAiUploadDataService qiFuAiUploadDataService;

    @ApiOperation(value = "奇富AI上传数据接入接口")
    @PostMapping("/uploadData/24152")
    @LogAnnotation
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public QiFuAiResDTO qiFuAiUploadData(@RequestBody QiFuAiReqDTO requestBody) {
        Pair<CodeEnum, FlagEnum> pair = qiFuAiUploadDataService.handle(requestBody);

        QiFuAiResDTO qiFuAiResDTO = new QiFuAiResDTO();
        qiFuAiResDTO.setCode(pair.getKey().getCode());
        qiFuAiResDTO.setMsg(pair.getKey().getDesc());
        qiFuAiResDTO.setFlag(pair.getValue().toString());
        qiFuAiResDTO.setData(new QiFuAiResDTO.DataResult());
        return qiFuAiResDTO;
    }
}
