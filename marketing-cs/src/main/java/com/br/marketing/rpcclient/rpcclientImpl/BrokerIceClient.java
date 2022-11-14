package com.br.marketing.rpcclient.rpcclientImpl;

import Ice.AsyncResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.bsf.ext.app.util.Ice1BSFConsumerBean;
import com.br.marketing.entity.RequestLog;
import com.br.mom.v3.broker_layer_api.api.BrokerLayerServicePrx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Slf4j
public class BrokerIceClient {

    /*上传日志app名称*/
    private static String upload_producerKey;

    /*上传日志mom加密key*/
    private static String upload_appSecretKey;

    /*上传日志db表名*/
    private static String upload_destinationName;


    /*推送日志app名称*/
    private static String push_appName;

    /*推送日志mom加密key*/
    private static String push_appSecretKey;

    /*推送日志db表名*/
    private static String push_destinationName;

    private static int upload_logIceTimeout;

    @Value("${otherConfig.uploadMom.producerKey:00}")
    public void setUpload_producerKey(String upload_producerKey) {
        BrokerIceClient.upload_producerKey = upload_producerKey;
    }

    @Value("${otherConfig.uploadMom.appSecretKey:00}")
    public void setUpload_appSecretKey(String upload_appSecretKey) {
        BrokerIceClient.upload_appSecretKey = upload_appSecretKey;
    }

    @Value("${otherConfig.uploadMom.destinationName:00}")
    public void setUpload_destinationName(String upload_destinationName) {
        BrokerIceClient.upload_destinationName = upload_destinationName;
    }

    @Value("${otherConfig.uploadMom.logIceTimeout:00}")
    public void setUpload_logIceTimeout(int upload_logIceTimeout) {
        BrokerIceClient.upload_logIceTimeout = upload_logIceTimeout;
    }

    @Value("${otherConfig.mom.appName:00}")
    public void setPush_appName(String push_appName) {
        BrokerIceClient.push_appName = push_appName;
    }

    @Value("${otherConfig.mom.appSecretKey:00}")
    public void setPush_appSecretKey(String push_appSecretKey) {
        BrokerIceClient.push_appSecretKey = push_appSecretKey;
    }

    @Value("${otherConfig.mom.destinationName:00}")
    public void setPush_destinationName(String push_destinationName) {
        BrokerIceClient.push_destinationName = push_destinationName;
    }

    /**
     * 推送上传日志
     *
     * @param content
     */
    public static void sendUploadLog(String content) {
        String param = null;
        try {
            BrokerLayerServicePrx service = (BrokerLayerServicePrx) Ice1BSFConsumerBean
                    .getServiceProxy(BrokerLayerServicePrx.class, "V3.0.0");
            //超时时间
            service.ice_invocationTimeout(upload_logIceTimeout);
            //请求参数
            JSONObject paramJson = new JSONObject();
            paramJson.put("appName", upload_producerKey);
            paramJson.put("appSecretKey", upload_appSecretKey);
            JSONObject requestData = new JSONObject();
            requestData.put("destinationName", upload_destinationName);
            //入参内容
            requestData.put("content", content);
            paramJson.put("requestData", requestData);
            paramJson.put("swiftNum", UUID.randomUUID().toString().replaceAll("-",""));
            param = paramJson.toJSONString();
            AsyncResult beginSender = service.begin_sender(param);
            log.warn("userReportLog mom request return : {}", beginSender == null ? "" : beginSender.isSent());
        } catch (Exception e) {
            log.error("userReportLog mom request Error：{}" + param, e);
        }
    }

    /**
     * 推送调用下游接口日志
     *
     * @param requestLog
     */
    public static void sendRequestLog(RequestLog requestLog) {
        JSONObject paramJson = new JSONObject();
        JSONObject requestData = new JSONObject();
        requestData.put("destinationName", push_destinationName);
        paramJson.put("appName", push_appName);
        paramJson.put("appSecretKey", push_appSecretKey);
        BrokerLayerServicePrx service = (BrokerLayerServicePrx) Ice1BSFConsumerBean.getServiceProxy(BrokerLayerServicePrx.class, "V3.0.0");
        service = (BrokerLayerServicePrx) service.ice_connectionCached(false);
        String param = JSON.toJSONString(requestLog);
        paramJson.put("swiftNum", UUID.randomUUID());
        requestData.put("content", param);
        paramJson.put("requestData", requestData);
        //log.warn("MQ入参--{}",paramJson);
        try {
            AsyncResult asyncResult = service.begin_sender(paramJson.toString());
            log.info("userReportLog mom request return : {}", asyncResult == null ? "" : asyncResult.isSent());
        } catch (Exception e) {
            log.error("日志信息写入消息队列异常", e);
        }
    }
}
