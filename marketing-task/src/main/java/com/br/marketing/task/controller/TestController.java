package com.br.marketing.task.controller;

import com.br.marketing.entity.Customer;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.service.Impl.CheckServicePackageImpl;
import com.br.marketing.task.service.Impl.LoanWarningServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

    @Autowired
    LoanWarningServiceImpl loanWarningService;

    @Resource
    CustomerMapper customerMapper;

    @GetMapping("/task")
    public String task(){
        Customer customerByApiCode = customerMapper.getCustomerByApiCode("7410571");
        loanWarningService.process(customerByApiCode,null);
        return "";
    }
}
