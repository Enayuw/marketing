package com.br.marketing.check.controller;

import com.br.marketing.check.service.PushFinishService;
import com.br.marketing.check.service.ResultCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: Bairong
 * @Time: 2020/12/21 16:00
 * @Company：百融
 * @Description: 功能描述
 */

@RestController
@RequestMapping("/finish/")
@Slf4j
public class PushFinishController {

    @Resource
    PushFinishService pushFinishServiceImpl;
    @GetMapping("put")
    public String put(String apiCode){
        pushFinishServiceImpl.pushFinish(apiCode);
        return "success";
    }
}
