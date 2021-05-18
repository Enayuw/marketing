package com.br.marketing.api.service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.alarm.AlarmApiClient;
import com.br.marketing.common.utils.net.IpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 报警服务
 */
@Service("alarmCS1")
public class AlarmCs {
    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;

    @Value("${otherConfig.alarm.sendCode:00}")
    private String sendCode;




    private String errorToString(Throwable e) {
        StringBuilder stringBuilder = new StringBuilder();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        stringBuilder.append(e.getMessage()).append("</br>");
        for (int index = 0; index < stackTraceElements.length; index++) {
            stringBuilder.append("       ").append(stackTraceElements[index].getClassName()).append(".");
            stringBuilder.append(stackTraceElements[index].getMethodName());
            stringBuilder.append("(")
                    .append(stackTraceElements[index].getFileName()).append(":")
                    .append(stackTraceElements[index].getLineNumber())
                    .append(")</br>");
        }
        return  stringBuilder.toString();
    }


    /**
     * 邮件内容处理
     * @param throwable 可抛出异常
     * @param type 类型
     * @param appName appName
     */
    public void handle(Throwable throwable, String type, String appName) {
        String methodName="";
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for(StackTraceElement ste:stackTrace){
            if(ste.getMethodName().startsWith("com.br")){
                methodName=ste.getMethodName();
                break;
            }
        }
        JSONObject json=new JSONObject();
        String hostName=IpUtil.getHostName();
        if("api".equals(type)){
            json.put("host", hostName);
            json.put("port", "18704");
            json.put("serverName", "LOAN-WARNING-API");
        }else if("web".equals(type)){
            json.put("host",hostName);
            json.put("port", "18703");
            json.put("serverName", "LOAN-WARNING-SERVICE");
        }
        else if("shell".equals(type)){
            json.put("serverName", "流失预警数据入库");
        }
        json.put("callMethod",methodName);
        json.put("type", "INTERFACE_SYS");
        json.put("message", throwable.getMessage());
        json.put("data", null);
        json.put("errorDetail",  errorToString(throwable));
        alarmClient.sendAlarm(json.toString(),"流失预警"+type+"内部任务报警",appName,secretKey,sendCode);
    }

}
