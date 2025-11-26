package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.service.mock.MockService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName MockRedisController
 * @Description 查询Mock挡板配置信息
 * @Author kongbx
 * @Date 2025/10/9 15:27
 */
@RestController
@Configuration
@RequestMapping("/mockRedis")
@Slf4j
public class MockRedisController {

    @Resource(name = "newMockService")
    private MockService mockService;

    private static final Integer CODE_1 = Integer.valueOf(1);
    /**
     * 查询Mock挡板配置信息
     *
     * @param cacheKey
     * @return
     */
    @Operation(summary = "查询Mock挡板配置信息")
    @PostMapping("/queryMockConfig")
    public ApiResult<String> queryMockConfig(@RequestParam("cacheKey") String cacheKey) {
        return new ApiResult<String>().fromResult(mockService.queryMockConfig(cacheKey), CODE_1);
    }

}
