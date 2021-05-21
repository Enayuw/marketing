package com.br.marketing.task.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
public class TestController {

    @Value("${myenv}")
    private String myenv;

    @GetMapping("/index")
    public String index(){
        return "hello word".concat(myenv);
    }
}
