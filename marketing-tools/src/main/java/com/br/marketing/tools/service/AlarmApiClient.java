//package com.br.marketing.tools.service;
//
//import com.alibaba.fastjson.JSONObject;
//import com.br.bsf.ext.app.util.Ice2BSFConsumerBean;
//import com.br.ice.service.alarm.BrSendAlarmNewServicePrx;
//import com.br.marketing.common.utils.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.stereotype.Service;
//
//import java.util.Locale;
//import java.util.Random;
//import java.util.UUID;
//
///**发送邮件客户端
// * @author 10400
// * @create 2017-06-27 13:33
// */
//@Service
//public class AlarmApiClient {
//    @Value("${otherConfig.alarm.secretKey:00}")
//    private String secretKey;
//
//
//    private static final Logger log = LoggerFactory.getLogger(AlarmApiClient.class);
//
//
//    public void send(final String title,final String mailContent,final String mails){
//        String result ="";
//        try {
//            JSONObject config = new JSONObject();
//            JSONObject requestData = new JSONObject();
//            requestData.put("onlyCode", new Random().nextInt(10000)+50000);
//            requestData.put("alarmLevel","1");
//            requestData.put("alarmType","2");
//            requestData.put("exceptionCode", new Random().nextInt(10000)+50000);
//            requestData.put("mailTitle", title);
//            requestData.put("mailContent",mailContent);
//            requestData.put("mails", mails);
//            requestData.put("sendType","1");
//            requestData.put("autograph","1");
//            config.put("appName","marketing");
//            config.put("appSecretKey",secretKey);
//            config.put("swiftNum", UUID.randomUUID().toString());
//            config.put("requestData",requestData);
//            BrSendAlarmNewServicePrx service= (BrSendAlarmNewServicePrx) Ice2BSFConsumerBean.getServiceProxy(BrSendAlarmNewServicePrx.class,"V3.0.0");
//            service= (BrSendAlarmNewServicePrx) service.ice_connectionCached(false);
//            log.info("bean--{}",config);
//            result = service.sendMessageToPresonal(config.toJSONString(),mailContent);
//            log.info("【mail send result】:{}",result);
//        } catch (Exception e) {
//            log.error("发送邮件报错：", e);
//        }
//        log.info("预警邮件发送结束!!返回结果{}",result);
//    }
//
//
//}
