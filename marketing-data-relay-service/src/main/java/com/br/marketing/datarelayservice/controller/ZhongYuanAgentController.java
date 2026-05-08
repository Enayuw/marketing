package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.service.ZhongYuanAgentService;
import com.br.marketing.dto.zhongyuan.MtStandardResponse;
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
 * @ClassName ZhongYuanAgentController
 * @Description 中原消金定制化上传-3700378
 * https://c.100credit.cn/pages/viewpage.action?pageId=245374409
 * @Author kongbx
 * @Date 2026/4/21 20:50
 */
@Tag(name = "中原消金")
@RequestMapping("/v1/task")
@RestController
@Slf4j
public class ZhongYuanAgentController {

    @Resource
    private ZhongYuanAgentService zhongYuanAgentService;

    @Operation(summary = "批量导入数据接口")
    @PostMapping("/importAgentCustomer")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public MtStandardResponse importAgentCustomer(@RequestBody String jsonData, HttpServletRequest request) {
        return zhongYuanAgentService.importAgentCustomer(jsonData, request);
    }
}
