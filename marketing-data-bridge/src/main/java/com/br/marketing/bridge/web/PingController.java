package com.br.marketing.bridge.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 心跳检测
 */
@RestController
public class PingController {

    /**
     * ping接口
     * @return 当前时间戳
     */
    @GetMapping({"/ping"})
    public String ping() {
        return "pong-" + System.currentTimeMillis();
    }
}
