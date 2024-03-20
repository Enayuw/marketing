package com.br.marketing.xcloop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Bairong on 2020/1/15.
 */
@RestController
public class PingController {
    @GetMapping({"/ping"})
    public String ping() {
        return "pong-" + System.currentTimeMillis();
    }
}
