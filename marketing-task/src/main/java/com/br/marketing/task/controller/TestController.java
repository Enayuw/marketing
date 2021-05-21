package com.br.marketing.task.controller;

import com.br.marketing.service.Impl.CheckServicePackageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
public class TestController {

    @Value("${myenv}")
    private String myenv;

    @Autowired
    CheckServicePackageImpl checkServicePackage;

    @GetMapping("/index")
    public String index(){
        String s = checkServicePackage.checkCsPackage();
        return "hello word".concat(myenv).concat("=====").concat(s);
    }
}
