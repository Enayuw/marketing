package com.br.marketing.api.controller;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guangchao.zhang
 * @Classname TestDataSendController
 * @Description 第三方接口数据接受模拟接口
 * @Date 2022/2/18 1:54 PM
 */
@RestController
@RequestMapping("/testDataSend/")
@Slf4j
public class TestDataSendController {

    @PostMapping("receiveData")
    public Map<String, String> receiveData(@RequestBody String data){
        log.warn("模拟第三方接受的数据为：{}",data);
        Map map = new HashMap();
        map.put("code","00");
        map.put("message","成功");
        return  map;
    }
}
