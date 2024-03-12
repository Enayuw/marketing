package com.br.marketing.tools.controller;

import com.br.marketing.client.net.ApiCaller;
import com.br.marketing.tools.job.DbMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    DbMonitor dbMonitor;

    @GetMapping("/testlogin")
    public String testLogin(){
        dbMonitor.testUrlSql();
        return "123";
    }

    @GetMapping("/testlogin2")
    public String testLogin2(){
        dbMonitor.slowDbSql();
        return "123";
    }


    @GetMapping("/testFilterSort")
    public String testFilterSort(){
        return "123";
    }

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/testCon")
    public String testCon(){
        String url = "http://101.42.10.86:18704/connectPool/acceptReq";
        String s = new ApiCaller(restTemplate).setUrl(url).get();
        return s;
    }

    @GetMapping("/testConPool")
    public String testConPool(){
        String url = "http://101.42.10.86:18704/connectPool/acceptSlowReq";
        String s = new ApiCaller(restTemplate).setUrl(url).get();
        return s;
    }
}
