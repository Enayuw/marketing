package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.service.ZhongYuanUploadDataService;
import com.br.marketing.dto.zhongyuan.ZhongYuanBaseResponse;
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
 * @ClassName ZhongYuanCustomizeController
 * @Description 中原消金-3760019（营销）:https://c.100credit.cn/pages/viewpage.action?pageId=227790485
 * @Author kongbx
 * @Date 2025/11/14 11:12
 */
@Tag(name = "中原消金")
@RequestMapping("/api/znwh")
@RestController
@Slf4j
public class ZhongYuanCustomizeController {

    @Resource
    private ZhongYuanUploadDataService zhongYuanUploadDataService;

    @Operation(summary = "用户登录接口")
    @PostMapping("/poc/task/login")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ZhongYuanBaseResponse<?> login(@RequestBody String jsonData, HttpServletRequest request) {
        return zhongYuanUploadDataService.login(jsonData, request);
    }

    @Operation(summary = "外呼上报接口")
    @PostMapping("/poc/batchTask")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ZhongYuanBaseResponse<?> batchTask(@RequestBody String jsonData, HttpServletRequest request) {
        return zhongYuanUploadDataService.batchTask(jsonData, request);
    }

    @Operation(summary = "场景变量信息接口")
    @PostMapping("/poc/sceneVariable")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ZhongYuanBaseResponse<?> sceneVariable(@RequestBody String jsonData, HttpServletRequest request) {
        return zhongYuanUploadDataService.sceneVariable(jsonData, request);
    }

    @Operation(summary = "批量外呼任务状态修改接口")
    @PostMapping("/poc/task/status")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ZhongYuanBaseResponse<?> status(@RequestBody String jsonData, HttpServletRequest request) {
        return zhongYuanUploadDataService.status(jsonData, request);
    }

    @Operation(summary = "外呼任务场景变量修改接口")
    @PostMapping("/task/change/sceneVariable")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ZhongYuanBaseResponse<?> changeSceneVariable(@RequestBody String jsonData, HttpServletRequest request) {
        return zhongYuanUploadDataService.changeSceneVariable(jsonData, request);
    }

}
