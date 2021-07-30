package com.br.marketing.api.controller;

import com.br.marketing.client.RedisChgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("redis")
public class RedisController {

    @Autowired
    RedisChgService redisChgService;

    @GetMapping("get")
    public String get(@RequestParam("key") String key){
        return redisChgService.get(key);
    }

    @GetMapping("del")
    public String del(@RequestParam("key") String key){
        long del = redisChgService.del(key);
        return String.valueOf(del);
    }

    @GetMapping("set")
    public String set(@RequestParam("key") String key,@RequestParam("value")  String value){
        redisChgService.set(key,value);
        return "success";
    }
}
