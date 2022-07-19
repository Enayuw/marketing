package com.br.marketing.tools.controller;

import com.br.marketing.tools.job.DbMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    DbMonitor dbMonitor;

    @GetMapping("/testlogin")
    public String testLogin(){
        dbMonitor.slowDbSql();
        return "123";
    }
}
