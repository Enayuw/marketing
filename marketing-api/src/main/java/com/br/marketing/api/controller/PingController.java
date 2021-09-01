package com.br.marketing.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 心跳检测
 */
@RestController
@RequestMapping("/ping")
public class PingController {

    /**
     * ping接口
     *
     * @return 当前时间戳
     */
    @GetMapping
    public String ping() {
        return "pong-" + System.currentTimeMillis();
    }
}
