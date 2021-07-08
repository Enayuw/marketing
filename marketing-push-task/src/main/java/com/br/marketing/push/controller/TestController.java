package com.br.marketing.push.controller;

import com.br.marketing.push.service.FlowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("test")
public class TestController {

    @Resource
    private FlowService flowService;

    @GetMapping({"/ping"})
    public String testMerge() {
        flowService.flow("7410433");
        return "ssss";
    }
}
