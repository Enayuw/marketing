package com.br.marketing.check.controller;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.service.DataService;
import com.br.marketing.service.IApiToDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/data/")
@Slf4j
public class DataController {

    @Resource
    DataService dataServiceImpl;

    @Autowired
    IApiToDbService iApiToDbService;

    @GetMapping("dataEliminate")
    public String dataEliminate(){
        boolean result=dataServiceImpl.dataEliminate();
        if(!result){
            return "fail";
        }
        return "success";
    }

    @GetMapping("testApiToDb")
    public String testApiToDb(){
        iApiToDbService.pushToDb();
        return "success";
    }

    @PostMapping("put")
    public String put(@RequestBody String data){
        log.warn("推送请求数据-{}",data);
        JSONObject result =new JSONObject();
        result.put("code","00");
        result.put("message","成功");
        return result.toJSONString();
    }

}
