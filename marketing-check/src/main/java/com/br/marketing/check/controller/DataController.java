package com.br.marketing.check.controller;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.service.DataService;
import com.br.marketing.entity.DataTest;
import com.br.marketing.mapper.DataTestMapper;
import com.br.marketing.service.IApiToDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping("/data/")
@Slf4j
public class DataController {

    @Resource
    DataService dataServiceImpl;

    @Autowired
    IApiToDbService iApiToDbService;
    @Resource
    DataTestMapper dataTestMapper;
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
    @PostMapping("pushTest")
    public String pushTest(@RequestBody String data){
        JSONObject result =new JSONObject();
        DataTest dataTest = new DataTest();
        dataTest.setCreateTime(new Date());
        dataTest.setData(data);
        dataTestMapper.insertSelective(dataTest);
        result.put("code","00");
        result.put("message","接收成功");
        return result.toJSONString();
    }
}
