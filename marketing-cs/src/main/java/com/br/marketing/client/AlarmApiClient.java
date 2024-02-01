package com.br.marketing.client;

import ch.qos.logback.classic.spi.ThrowableProxy;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.net.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.*;

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
     * @param exceptionCode
     */
    public void sendAlarm(String content, String title, String exceptionCode){
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
    public void sendAlarmPrintStack(String content, String title,String exceptionCode, ThrowableProxy throwableProxy){
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
            if(throwableProxy!=null){
                log.warn(msg,new Exception(throwableProxy.getThrowable()));
            }else {
                log.warn(msg);
            }

        }catch (Exception e){
            String msg = AlertLog.buildWarnMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), "", "发送邮件异常");
            log.warn(msg);
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

    public static String sendMails(String mailTitle, String mailContent, String mails) {
        String result = "";
        try {
            result = AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATA_GOVERNANCE_PLATFORM_SEND_EMAIL.getCode(),
                    mailContent, mailTitle, Arrays.asList(mails), new ArrayList(), new ArrayList());
            log.warn(result);
        } catch (Exception var5) {
            log.warn("mailTitle:[{}]mailContent:[{}]mails:[{}]--buildWarnMessageException", new Object[]{mailTitle, mailContent, mails, var5});
        }

        return result;
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
