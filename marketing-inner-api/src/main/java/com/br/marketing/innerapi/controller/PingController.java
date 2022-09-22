package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
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

    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

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
        json.put("message", "测试报警接口");
        sendAlarm(json.toString(),"调用了测试报警接口,请忽略~",appName,secretKey, "1001");
        return "111";
    }

    private void sendAlarm(String content, String title, String appName, String secretKey, String exceptionCode) {
        String enviroment= "预发";
        String hostName = IpUtil.getHostName();
        if(StringUtils.isNotEmpty(title)){
            title="【"+enviroment+"】"+hostName +title;
        }else{
            title ="【"+enviroment+"】"+hostName+ JSONObject.parseObject(content).getString("serverName");
        }
        try{
            //生成打告警标签的消息内容
            //其中 AlertLog 只是为日志的内容添加了标签，打印日志时还是按照正常方式输入参数、异 常。
            String msg = AlertLog.buildWarnMessage(exceptionCode, content, title);
            log.warn(msg, 10, new RuntimeException("调用了测试报警接口,请忽略~"));
        }catch (Exception e){
            String msg = AlertLog.buildWarnMessage(exceptionCode, content, title);
            log.warn(msg,10, new RuntimeException("调用新报警发生异常！"+e));
        }
    }


}
