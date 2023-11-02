package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.Impl.ProductResultByConfigSimpleServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.UserCenterHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("redis")
@Slf4j
public class RedisController {

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    IProductResultSimpleService productResultSimpleService;

    @Autowired
    UserCenterHandler userCenterHandler;

    @GetMapping("testM")
    public String testM(){
        String ms =  "{\"apiCode\":\"7479978\",\"operateType\":\"add\"}";
        userCenterHandler.handleDataUserCenter(ms);
        return "";
    }

    @GetMapping("get")
    public String get(@RequestParam("key") String key,@RequestParam(value = "type",required = false) String type) {
        if(!redisChgService.exists(key)){
            return "key不存在";
        }
        if("Set".equals(type)){
            return JSON.toJSONString(redisChgService.smembers(key));
        }else{
            return redisChgService.get(key);
        }
    }

    @GetMapping("del")
    public String del(@RequestParam("key") String key) {
        long del = redisChgService.del(key);
        return String.valueOf(del);
    }

    @GetMapping("set")
    public String set(@RequestParam("key") String key, @RequestParam("value") String value) {
        redisChgService.set(key, value);
        return "success";
    }

    @GetMapping("/clearInnerCache")
    public String clearInnerCache(@RequestParam("type") Integer type){
        if(Integer.valueOf(1).equals(type)){
            ProductResultByConfigSimpleServiceImpl.flagScoreByinnerList.clear();
        }
        return "success";
    }

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @GetMapping("/test")
    public String test(){
        String ab = "{\"apiCode\":\"7410438\",\"jsonData\":{\"accessNumber\":\"juman_20220905_01\",\"batchNumber\":\"juman_20220905\",\"data\":[{\"caseNumber\":\"20220905_01\",\"phone\":\"AgsNΒ7VlVSWwkAVwY\",\"variables\":{\"groupType\":\"促首登\",\"score\":\"83.0\",\"scoreDate\":\"2021-07-26\",\"scoreName\":\"scorencashonshcdlyxf\",\"taskId\":\"82021072601\",\"update\":\"\",\"sleepGroup\":\"540+\"}}],\"extendData\":{\"sampleTotal\":\"1\",\"scoreName\":\"scorencashonshcdlyxf\"},\"method\":\"caseAdd\"},\"platApiCode\":\"7410438\"}";
        PushMarketingUserDTO o = JSON.parseObject(ab, new TypeReference<PushMarketingUserDTO>() {
        }.getType());
        Result<Integer> integerResult = intelligentCustomerServiceClient.pushUser(o, 123L, "123",1);
        System.out.println(integerResult.getMessage());
        return "";
    }

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    /**
     * 获取speed的配置信息 方便验证speed是否推送成功
     *
     * @return
     */
    @GetMapping("/getSpeedInfo")
    public String getSpeedInfo() {
        return marketingCommonConfig.toString();
    }

    
}
