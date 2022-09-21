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
        json.put("message", "接口调用报警");
        sendAlarm(json.toString(),"营销平台"+"MARKETING-INNER-API"+"测试报警",appName,secretKey, Constants.sendCodeMap.get("sysError"));
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
            //BrSendAlarmNewServicePrx service = (BrSendAlarmNewServicePrx) Ice2BSFConsumerBean.getServiceProxy(BrSendAlarmNewServicePrx.class,"V3.0.0");
            //service= (BrSendAlarmNewServicePrx) service.ice_connectionCached(false);
            //sendMailData(content,title,appName,secretKey,exceptionCode,service);
            //生成打告警标签的消息内容
            //【预发】marketing-inner-api-778459df56-22m58营销平台MARKETING-INNER-API测试报警
            String msg = AlertLog.buildWarnMessage("1001", content);
            //将生成的消息打印输出
            log.warn(msg, 10, new RuntimeException("1001测试告警"));
            //其中 AlertLog 只是为日志的内容添加了标签，打印日志时还是按照正常方式输入参数、异 常。
            String msg2 = AlertLog.buildWarnMessage("1002", content, title+"测试邮件主题");
            log.warn(msg2, 10, new RuntimeException("1002测试邮件告警"));
        }catch (Exception e){
            String msg2 = AlertLog.buildWarnMessage("1002", content, title+"发送邮件异常");
            log.error(msg2,10, new RuntimeException("发送邮件异常"+e));
        }
    }


}
