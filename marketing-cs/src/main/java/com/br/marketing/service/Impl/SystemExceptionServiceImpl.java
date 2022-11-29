package com.br.marketing.service.Impl;

import ch.qos.logback.classic.spi.ThrowableProxy;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.net.IpUtil;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 系统异常预警
 * Created by Bairong on 2020/7/11.
 */
@Service
public class SystemExceptionServiceImpl implements EmailService {
    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    public void sendAlarm(String context,  String type) {
        JSONObject json=new JSONObject();
        json.put("host", IpUtil.getHostName());
        json.put("serverName", type);
        json.put("message", context);
        alarmClient.sendAlarm(json.toString(),"营销平台"+type+"内部系统异常报警", AlarmSendCodeEnum.ERROR_UNKNOWN.getCode());
    }

    public void sendAlarmPrintStack(String context, String type, ThrowableProxy throwableProxy) {
        JSONObject json=new JSONObject();
        json.put("host", IpUtil.getHostName());
        json.put("serverName", type);
        json.put("message", context);
        alarmClient.sendAlarmPrintStack(json.toString(),"营销平台"+type+"内部系统异常报警",AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(),throwableProxy);
    }

    @Override
    public void hxResultErrorAlarm(String title, String message) {
        alarmClient.sendAlarm(message,title,AlarmSendCodeEnum.ERROR_UNKNOWN.getCode());
    }

    @Override
    public void zipFileErrorAlarm(String fileName, String title) {

    }

    @Override
    public void closeDateAlarm() {

    }

    @Override
    public void monitoringExpirationAlarm() {

    }


    public void sendReport(String apiCode) {

    }


    public void resultVolumeCheck(String apiCode) {

    }

    @Override
    public void resultVolumeCheck(StraHisFile file) {

    }

    public void ftpToSftpCheck(String apiCode) {

    }


    public void fileSizeException(String apiCode, String message) {

    }


    public void fileUploadFtpException(String apiCode, String message) {

    }




    public void report() {

    }


    public void fileUpload(String apiCode, String message) {

    }


    public void dataFileVolumn(String apiCode, String message) {

    }

    @Override
    public void progressReport() {

    }

    @Override
    public void deleteMonitorFileUpload(String apiCode, String message) {

    }


}
