package com.br.marketing.check.controller;

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
@RequestMapping("/result/")
@Slf4j
public class ResultCheckController {

    @Resource
    ResultCheckService resultCheckServiceImpl;
    @GetMapping("check")
    public String check(String apiCode){
        resultCheckServiceImpl.taskResultCheck(apiCode);
        return "success";
    }
}
