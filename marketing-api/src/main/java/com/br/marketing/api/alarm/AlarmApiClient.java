package com.br.marketing.api.alarm;

import com.alibaba.fastjson.JSONObject;
import com.br.bsf.ext.app.util.Ice2BSFConsumerBean;
import com.br.ice.service.alarm.BrSendAlarmNewServicePrx;
import com.br.marketing.api.StrategyEarlyWaringApiApplication;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.net.IpUtil;
import com.br.marketing.common.utils.transaction.SwiftNumberManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The type Alarm api client.
 */
@Service
public class AlarmApiClient  {
    private static final Logger log = LoggerFactory.getLogger(AlarmApiClient.class);

    private static String PROD="prod";
    private static String PRE="pre";
    private static String DEV="dev";
    /**
     * 发送邮件
     * @param content 邮件内容
     * @param title  邮件标题
     * @param appName 应用名称
     * @param secretKey 秘钥
     * @param exceptionCode 发送码
     * @param mails 邮箱地址（只有自定义发送时需要，参数格式为‘***@100credit.com,###@100credit.com’。不传用默认方式发送）
     * @return
     */
    public void sendAlarm(String content, String title, String appName, String secretKey, String exceptionCode, String...mails){
        String activeEnv=getActiveProfile();
        String enviroment ="";
        if(PROD.equals(activeEnv)){
            enviroment= "生产";
        }else if(PRE.equals(activeEnv)){
            enviroment= "预发";
        }else if(DEV.equals(activeEnv)){
            enviroment= "测试";
        }
        String env="【"+enviroment+"】"+ IpUtil.getHostName();
        if(StringUtils.isNotEmpty(title)){
            title=env+title;
        }else{
            title =env+ JSONObject.parseObject(content).getString("serverName");
        }
        BrSendAlarmNewServicePrx service = (BrSendAlarmNewServicePrx) Ice2BSFConsumerBean.getServiceProxy(BrSendAlarmNewServicePrx.class,"V3.0.0");
        sendMailData(content,title,appName,secretKey,exceptionCode,service);
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
    private  void sendMailData(String content, String title, String appName, String secretKey,
                               String exceptionCode, BrSendAlarmNewServicePrx service){
        try {
            String swiftNumber = SwiftNumberManager.generate();
            log.info("调用报警服务开始,流水号：{},报警邮件标题：{},异常码：{}",swiftNumber,title,exceptionCode);
            JSONObject alarmObj = new JSONObject();
            JSONObject requestData = new JSONObject();
            requestData.put("mailTitle", title);
            alarmObj.put("appSecretKey", secretKey);
            alarmObj.put("swiftNum",swiftNumber);
            alarmObj.put("appName",appName);
            requestData.put("mailContent",dealTemplate(content));
            requestData.put("sendCode",exceptionCode);
            alarmObj.put("requestData",requestData);
            log.info("调用报警服务请求参数：{}",alarmObj);
            String result = service.sendAlarm(alarmObj.toJSONString());
            log.info("调用报警服务结束，流水号{},返回结果为{}",swiftNumber,result);
        } catch (Exception e1) {
            log.error("send mail is error --", e1);
        }
    }

    /**
     * 处理邮件内容
     * @param content
     * @return
     */
    private String dealTemplate(String content){
        StringBuilder ext = new StringBuilder();
        if(StringUtils.isNotEmpty(content)){
            JSONObject contentJson=JSONObject.parseObject(content);
            ext.append("<h3><b>报警内容：</b></h3>"
                            +"<strong>服务器地址</strong>："+contentJson.getString("host") + "<br/>"
                            +"<strong>端口</strong>："+contentJson.getString("port") + "<br/>"
                            +"<strong>服务名称</strong>："+contentJson.getString("serverName")+ "<br/>"
                            +"<strong>服务类型</strong>："+contentJson.getString("type")+ "<br/>"
                            +"<strong>出错方法</strong>："+contentJson.getString("callMethod") + "<br/>"
                            +"<strong>参数</strong>："+contentJson.getString("data") + "<br/>"
                            +"<strong>错误提示信息</strong>："+contentJson.getString("message") + "<br/>"
                            +"<strong>错误详情</strong>："+contentJson.getString("errorDetail").replaceAll(System.lineSeparator(),"<br/>") + "<br/>"
            );
        }
      return  ext.toString();
    }
    /**
     * Gets active profile.
     *
     * @return the active profile
     */
    public  String getActiveProfile() {
        return StrategyEarlyWaringApiApplication.ac.getEnvironment().getActiveProfiles()[0];
    }
}
