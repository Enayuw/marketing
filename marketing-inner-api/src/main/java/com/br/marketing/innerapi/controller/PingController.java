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
            String msg = AlertLog.buildWarnMessage("1001", title+content+"测试告警。。。。");
            //将生成的消息打印输出
            log.warn(msg, 10, new RuntimeException("120测试告警"));
            //其中 AlertLog 只是为日志的内容添加了标签，打印日志时还是按照正常方式输入参数、异 常。
            String msg2 = AlertLog.buildWarnMessage("1002", content+"测试告警。。。。，当前平均查询时间为： 10 s", title+"测试邮件主题");
            log.warn(msg2, 10, new RuntimeException("110测试邮件告警"));
        }catch (Exception e){
            String msg2 = AlertLog.buildWarnMessage("1002", content+"测试告警。。。。，发送报警异常", title+"....");
            log.error(msg2,10, new RuntimeException("110测试邮件告警"+e));
        }
    }


}
