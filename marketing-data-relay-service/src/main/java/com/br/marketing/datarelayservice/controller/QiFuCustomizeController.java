package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.qifu.enums.CodeEnum;
import com.br.marketing.client.qifu.enums.FlagEnum;
import com.br.marketing.datarelayservice.client.QiFuAiReqDTO;
import com.br.marketing.datarelayservice.client.QiFuAiResDTO;
import com.br.marketing.datarelayservice.service.QiFuCustomizeService;
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
 * @ClassName QiFuCustomizeController
 * @Description 奇富360促动支相关
 * @Author kongbx
 * @Date 2025/6/9 14:03
 */
@Api(value = "QiFuCustomizeController")
@RequestMapping("/marketing/v1/actuation")
@RestController
@Slf4j
public class QiFuCustomizeController {

    @Resource
    private QiFuCustomizeService qiFuCustomizeService;

    @ApiOperation(value = "促动分析效果统计数据报表")
    @PostMapping("/analysisStatistics")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public QiFuAiResDTO analysisStatistics(@RequestBody QiFuAiReqDTO requestBody) {
        Pair<CodeEnum, FlagEnum> pair = qiFuCustomizeService.handle(requestBody, "actuation");

        QiFuAiResDTO qiFuAiResDTO = new QiFuAiResDTO();
        qiFuAiResDTO.setCode(pair.getKey().getCode());
        qiFuAiResDTO.setMsg(pair.getKey().getDesc());
        qiFuAiResDTO.setFlag(pair.getValue().toString());
        qiFuAiResDTO.setData(new QiFuAiResDTO.DataResult());
        return qiFuAiResDTO;
    }

}
