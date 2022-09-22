package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.utils.net.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 心跳检测
 */
@RestController
@RequestMapping("/ping")
public class PingController {

    @Resource
    private AlarmApiClient alarmClient;


    private static final Logger log = LoggerFactory.getLogger(PingController.class);

    /**
     * ping接口
     *
     * @return 当前时间戳
     */
    @GetMapping
    public String ping() {
        return "pong-" + System.currentTimeMillis();
    }

    @GetMapping("/logTest")
    public String logTest() {
        JSONObject json=new JSONObject();
        json.put("host", IpUtil.getHostName());
        json.put("serverName", "MARKETING-INNER-API");
        json.put("message", "测试报警内容 alarmClient.sendAlarm()");
        alarmClient.sendAlarm(json.toString(),"调用了测试报警接口,请忽略~",null,null, "1001");
        return "111";
    }

    @GetMapping("/logErrorTest")
    public String logErrorTest() {
        try{
            int i = 20 / 0;
        }catch (Exception e){
            log.error("测试报警接口，log.error()",e);
        }
        return "log.error()";
    }


}
