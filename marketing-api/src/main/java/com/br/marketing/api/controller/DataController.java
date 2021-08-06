package com.br.marketing.api.controller;

import com.alibaba.fastjson.JSONObject;
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

    @Autowired
    IApiToDbService iApiToDbService;
    @Resource
    DataTestMapper dataTestMapper;

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
