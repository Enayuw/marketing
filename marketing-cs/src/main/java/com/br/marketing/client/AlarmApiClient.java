package com.br.marketing.client;

import com.alibaba.fastjson.JSONObject;
import com.br.bsf.ext.app.util.Ice2BSFConsumerBean;
import com.br.common.log.AlertLog;
import com.br.ice.service.alarm.BrSendAlarmNewServicePrx;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.net.IpUtil;
import com.br.marketing.es.util.SwiftNumberManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**发送邮件客户端
 * @author 10400
 * @create 2017-06-27 13:33
 */
@Service
public class AlarmApiClient implements ApplicationContextAware {
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;


    private static ApplicationContext context = null;
    private static final Logger log = LoggerFactory.getLogger(AlarmApiClient.class);


    private static String PROD="prod";
    private static String PRE="pre";
    private static String DEV="dev";

    /**发送邮件
     * @param content
     * @param title
     * @param appName
     * @param secretKey
     * @param exceptionCode
     */
    public void sendAlarm(String content, String title, String appName, String secretKey, String exceptionCode){
        String activeEnv=getActiveProfile();
        String enviroment ="";
        if(DEV.equals(activeEnv) || PRE.equals(activeEnv)){
            enviroment= "预发";
        }else if(PROD.equals(activeEnv)){
            enviroment= "生产";
        }
        String hostName = IpUtil.getHostName();
        if(StringUtils.isNotEmpty(title)){
            title="【"+enviroment+"】"+hostName +title;
        }else{
            title ="【"+enviroment+"】"+hostName+ JSONObject.parseObject(content).getString("serverName");
        }
        try{
//            BrSendAlarmNewServicePrx service = (BrSendAlarmNewServicePrx) Ice2BSFConsumerBean.getServiceProxy(BrSendAlarmNewServicePrx.class,"V3.0.0");
//            service= (BrSendAlarmNewServicePrx) service.ice_connectionCached(false);
//            sendMailData(content,title,appName,secretKey,exceptionCode,service);
            String msg = AlertLog.buildWarnMessage(exceptionCode, content, title);
            log.warn(msg);
        }catch (Exception e){
            log.error("发送邮件异常", e);
        }

    }

    /**
     * 新报警平台未知错误，打印堆栈信息
     * @param content
     * @param title
     * @param exceptionCode
     */
    public void sendAlarmPrintStack(String content, String title,String exceptionCode){
        String activeEnv=getActiveProfile();
        String enviroment ="";
        if(DEV.equals(activeEnv) || PRE.equals(activeEnv)){
            enviroment= "预发";
        }else if(PROD.equals(activeEnv)){
            enviroment= "生产";
        }
        String hostName = IpUtil.getHostName();
        if(StringUtils.isNotEmpty(title)){
            title="【"+enviroment+"】"+hostName +title;
        }else{
            title ="【"+enviroment+"】"+hostName+ JSONObject.parseObject(content).getString("serverName");
        }
        try{
            String msg = AlertLog.buildWarnMessage(exceptionCode, content, title);
            log.warn(msg);
        }catch (Exception e){
            log.error("发送邮件异常", e);
        }
    }

    /**
     * 默认方式发送邮件
     * @param content
     * @param title
     * @param appName
     * @param secretKey
     * @param exceptionCode
     * @param service
     */
    private  void sendMailData(String content, String title, String appName, String secretKey, String exceptionCode,
                               BrSendAlarmNewServicePrx service){
        String swiftNumber = SwiftNumberManager.getSwiftNumberManager().getSwiftNumber();
        String result="";
        try {
            log.debug("调用报警服务开始,流水号：{},报警邮件标题：{},异常码：{}",swiftNumber,title,exceptionCode);
            JSONObject alarmObj = new JSONObject();
            JSONObject requestData = new JSONObject();
            requestData.put("mailTitle", title);
            alarmObj.put("appSecretKey", secretKey);
            alarmObj.put("swiftNum",swiftNumber);
            alarmObj.put("appName",appName);
            requestData.put("mailContent",dealTemplate(content));
            requestData.put("msgContent",dealTemplate(content));
            requestData.put("wechatContent",dealTemplate(content));
            requestData.put("sendCode",exceptionCode);
            alarmObj.put("requestData",requestData);
            log.debug("调用报警服务请求参数：{}",alarmObj);
             result = service.sendAlarm(alarmObj.toJSONString());
            log.debug("调用报警服务结束，流水号:{},返回结果为{}",swiftNumber,result);
        } catch (Exception e1) {
            log.error("send mail is error --", e1);
            log.error("调用报警服务结束，流水号:{},返回结果为{}",swiftNumber,result);
        }
    }
    /**
     * 处理邮件内容
     * @param content
     * @return
     */
    private String dealTemplate(String content){
        StringBuilder ext = new StringBuilder();
        try {
            if(StringUtils.isNotEmpty(content)){
                JSONObject contentJson=JSONObject.parseObject(content);
                ext.append("<h3><b>报警内容：</b></h3>")
                        .append("<strong>服务器地址</strong>：")
                        .append(contentJson.getString("host"))
                        .append("<br/>")
                        .append("<strong>服务名称</strong>：")
                        .append(contentJson.getString("serverName"))
                        .append("<br/>")
                        .append("<strong>错误提示信息</strong>：")
                        .append(contentJson.getString("message"))
                        .append("<br/>");
            }
        }catch (Exception e){
            return content;
        }
      return  ext.toString();
    }

    public void send(final String title,final String mailContent,final String mails){
        String result ="";
        try {
            JSONObject config = new JSONObject();
            JSONObject requestData = new JSONObject();
            requestData.put("onlyCode", new Random().nextInt(10000)+50000);
            requestData.put("alarmLevel","1");
            requestData.put("alarmType","2");
            requestData.put("exceptionCode", new Random().nextInt(10000)+50000);
            requestData.put("mailTitle", title);
            requestData.put("mailContent",mailContent);
            requestData.put("mails", mails);
            requestData.put("sendType","1");
            requestData.put("autograph","1");
            config.put("appName","marketing");
            config.put("appSecretKey",secretKey);
            config.put("swiftNum", UUID.randomUUID().toString());
            config.put("requestData",requestData);
            BrSendAlarmNewServicePrx service= (BrSendAlarmNewServicePrx) Ice2BSFConsumerBean.getServiceProxy(BrSendAlarmNewServicePrx.class,"V3.0.0");
            service= (BrSendAlarmNewServicePrx) service.ice_connectionCached(false);
            log.info("bean--{}",config);
            result = service.sendMessageToPresonal(config.toJSONString(),mailContent);
            log.info("【mail send result】:{}",result);
        } catch (Exception e) {
            log.error("发送邮件报错：", e);
        }
        log.info("预警邮件发送结束!!返回结果{}",result);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
    // 传入线程中
    public static <T> T getBean(String beanName) {
        return (T) context.getBean(beanName);
    }

    // 国际化使用
    public static String getMessage(String key) {
        return context.getMessage(key, null, Locale.getDefault());
    }

    /// 获取当前环境
    public static String getActiveProfile() {
        return context.getEnvironment().getActiveProfiles()[0];
    }

}
