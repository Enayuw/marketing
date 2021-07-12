package com.br.marketing.push.controller;

import com.br.marketing.entity.Customer;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.push.service.FlowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("/test")
public class TestController {

    @Resource
    CustomerMapper customerMapper;

    @Resource
    private FlowService flowService;

    @GetMapping({"/testMerge"})
    public String testMerge() {
        Customer customerByApiCode = customerMapper.getCustomerByApiCode("7410433");
        flowService.flow(customerByApiCode);
        return "ssss";
    }
}
