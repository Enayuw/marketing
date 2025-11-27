package com.br.marketing.xc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
import com.google.api.client.util.Lists;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/back")
@Slf4j
@Tag(name = "携程后门接口")
public class BackController {
    @Resource
    private XieChengRobDataCollidingService xieChengRobDataCollidingService;
    @Resource
    private RedisChgService redisChgService;

    @Operation(summary = "1-初始化当天release_time缓存")
    @GetMapping("initializeTodayReleaseTime")
    public ApiResult<List<Map<String, String>>> initializeTodayReleaseTime() {
        String today = DateUtil.today();
        String hkey = RedisKeyConstant.XIECHENG_RELEASE_TIME + today;
        xieChengRobDataCollidingService.initializeTodayReleaseTime(hkey);
        List<String> keys = redisChgService.hkeys(hkey);
        List<Map<String, String>> res = Lists.newArrayList();
        for (String key : keys) {
            Map<String, String> map = new HashMap<>();
            map.put(key, redisChgService.hget(hkey, key));
            res.add(map);
        }
        return new ApiResult<List<Map<String, String>>>().success(res);
    }
}