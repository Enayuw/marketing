package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.qifu.enums.CodeEnum;
import com.br.marketing.client.qifu.enums.FlagEnum;
import com.br.marketing.datarelayservice.client.QiFuAiReqDTO;
import com.br.marketing.datarelayservice.client.QiFuAiResDTO;
import com.br.marketing.datarelayservice.enums.QiFuAiBizTypeEnum;
import com.br.marketing.datarelayservice.service.QiFuAiUploadDataService;
import cn.hutool.core.lang.Pair;
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
 * @Description UploadDataController
 * @Author hong.chen
 * @CreateTime 2024/10/25
 */
@Tag(name = "UploadDataController", description = "UploadDataController")
@RequestMapping("/marketing/v1")
@RestController
@Slf4j
public class UploadDataController {
    @Resource
    private QiFuAiUploadDataService qiFuAiUploadDataService;

    private static final String TEST_API_CODE = "Test-ApiCode";

    @Operation(summary = "奇富AI上传数据接入接口")
    @PostMapping("/uploadData/24152")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public QiFuAiResDTO qiFuAiUploadData(@RequestBody QiFuAiReqDTO requestBody, HttpServletRequest request) {
        String testApiCode = request.getHeader(TEST_API_CODE);
        Pair<CodeEnum, FlagEnum> pair = qiFuAiUploadDataService.handle(requestBody, QiFuAiBizTypeEnum.UPLOAD_DATA.getType(),testApiCode);

        QiFuAiResDTO qiFuAiResDTO = new QiFuAiResDTO();
        qiFuAiResDTO.setCode(pair.getKey().getCode());
        qiFuAiResDTO.setMsg(pair.getKey().getDesc());
        qiFuAiResDTO.setFlag(pair.getValue().toString());
        qiFuAiResDTO.setData(new QiFuAiResDTO.DataResult());
        return qiFuAiResDTO;
    }

    @Operation(summary = "奇富AI语音机器人当月报表数据接入接口")
    @PostMapping("/uploadData/3700226")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public QiFuAiResDTO qiFuAiRobotReportUploadData(@RequestBody QiFuAiReqDTO requestBody, HttpServletRequest request) {
        String testApiCode = request.getHeader(TEST_API_CODE);
        Pair<CodeEnum, FlagEnum> pair = qiFuAiUploadDataService.handle(requestBody, QiFuAiBizTypeEnum.ROBOT_REPORT.getType(), testApiCode);

        QiFuAiResDTO qiFuAiResDTO = new QiFuAiResDTO();
        qiFuAiResDTO.setCode(pair.getKey().getCode());
        qiFuAiResDTO.setMsg(pair.getKey().getDesc());
        qiFuAiResDTO.setFlag(pair.getValue().toString());
        qiFuAiResDTO.setData(new QiFuAiResDTO.DataResult());
        return qiFuAiResDTO;
    }

    @Operation(summary = "奇富AI语音机器人排名报表推送接口")
    @PostMapping("/uploadData/ranking")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public QiFuAiResDTO qiFuAiRobotRankingReportUploadData(@RequestBody QiFuAiReqDTO requestBody, HttpServletRequest request) {
        String testApiCode = request.getHeader(TEST_API_CODE);
        Pair<CodeEnum, FlagEnum> pair = qiFuAiUploadDataService.handle(requestBody, QiFuAiBizTypeEnum.ROBOT_RANKING_REPORT.getType(), testApiCode);

        QiFuAiResDTO qiFuAiResDTO = new QiFuAiResDTO();
        qiFuAiResDTO.setCode(pair.getKey().getCode());
        qiFuAiResDTO.setMsg(pair.getKey().getDesc());
        qiFuAiResDTO.setFlag(pair.getValue().toString());
        qiFuAiResDTO.setData(new QiFuAiResDTO.DataResult());
        return qiFuAiResDTO;
    }

    @Operation(summary = "360AI语音机器人事件推送接口")
    @PostMapping("/uploadData/eventPush")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public QiFuAiResDTO qiFuAiRobotEventPushUploadData(@RequestBody QiFuAiReqDTO requestBody, HttpServletRequest request) {
        String testApiCode = request.getHeader(TEST_API_CODE);
        Pair<CodeEnum,FlagEnum> pair = qiFuAiUploadDataService.handle(requestBody, QiFuAiBizTypeEnum.ROBOT_EVENT_PUSH.getType(), testApiCode);

        QiFuAiResDTO qiFuAiResDTO = new QiFuAiResDTO();
        qiFuAiResDTO.setCode(pair.getKey().getCode());
        qiFuAiResDTO.setMsg(pair.getKey().getDesc());
        qiFuAiResDTO.setFlag(pair.getValue().toString());
        qiFuAiResDTO.setData(new QiFuAiResDTO.DataResult());
        return qiFuAiResDTO;
    }

    @Operation(summary = "360AI语音机器人效果推送接口")
    @PostMapping("/uploadData/effect")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public QiFuAiResDTO qiFuAiRobotEffectPushUploadData(@RequestBody QiFuAiReqDTO requestBody, HttpServletRequest request) {
        String testApiCode = request.getHeader(TEST_API_CODE);
        Pair<CodeEnum,FlagEnum> pair = qiFuAiUploadDataService.handle(requestBody, QiFuAiBizTypeEnum.ROBOT_EFFECT.getType(), testApiCode);

        QiFuAiResDTO qiFuAiResDTO = new QiFuAiResDTO();
        qiFuAiResDTO.setCode(pair.getKey().getCode());
        qiFuAiResDTO.setMsg(pair.getKey().getDesc());
        qiFuAiResDTO.setFlag(pair.getValue().toString());
        qiFuAiResDTO.setData(new QiFuAiResDTO.DataResult());
        return qiFuAiResDTO;
    }
}
