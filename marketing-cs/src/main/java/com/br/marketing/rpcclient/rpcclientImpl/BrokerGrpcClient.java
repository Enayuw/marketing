package com.br.marketing.rpcclient.rpcclientImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.grpc.mom.broker_layer_api.BrokerLayerGrpc;
import com.br.grpc.mom.broker_layer_api.SendRequest;
import com.br.marketing.common.utils.net.InterfaceLog;
import com.br.marketing.entity.RequestLog;
import com.br.marketing.rpcclient.GrpcClientInitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class BrokerGrpcClient {

    private static String upload_producerKey;

    private static String upload_appSecretKey;

    private static String upload_destinationName;

    private static int upload_logIceTimeout;

    /*推送日志app名称*/
    private static String push_appName;

    /*推送日志mom加密key*/
    private static String push_appSecretKey;

    /*推送日志db表名*/
    private static String push_destinationName;

    @Value("${otherConfig.uploadMom.producerKey:00}")
    public void setUpload_producerKey(String upload_producerKey) {
        BrokerGrpcClient.upload_producerKey = upload_producerKey;
    }

    @Value("${otherConfig.uploadMom.appSecretKey:00}")
    public void setUpload_appSecretKey(String upload_appSecretKey) {
        BrokerGrpcClient.upload_appSecretKey = upload_appSecretKey;
    }

    @Value("${otherConfig.uploadMom.destinationName:00}")
    public void setUpload_destinationName(String upload_destinationName) {
        BrokerGrpcClient.upload_destinationName = upload_destinationName;
    }

    @Value("${otherConfig.uploadMom.logIceTimeout:00}")
    public void setUpload_logIceTimeout(int upload_logIceTimeout) {
        BrokerGrpcClient.upload_logIceTimeout = upload_logIceTimeout;
    }

    @Value("${otherConfig.mom.appName:00}")
    public void setPush_appName(String push_appName) {
        BrokerGrpcClient.push_appName = push_appName;
    }

    @Value("${otherConfig.mom.appSecretKey:00}")
    public void setPush_appSecretKey(String push_appSecretKey) {
        BrokerGrpcClient.push_appSecretKey = push_appSecretKey;
    }

    @Value("${otherConfig.mom.destinationName:00}")
    public void setPush_destinationName(String push_destinationName) {
        BrokerGrpcClient.push_destinationName = push_destinationName;
    }

    public static void sendUploadLog(String content) {
        String param = null;
        try {
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
            sendFuture(param);
            log.warn("userReportLog mom request return：future");
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
        String param = JSON.toJSONString(requestLog);
        paramJson.put("swiftNum", UUID.randomUUID());
        requestData.put("content", param);
        paramJson.put("requestData", requestData);
        //log.warn("MQ入参--{}",paramJson);
        try {
            sendFuture(paramJson.toString());
            log.info("pushLog mom request return : future");
        } catch (Exception e) {
            log.error("日志信息写入消息队列异常", e);
        }
    }

    /**
     * 推送调用下游接口日志
     * @param interfaceLog
     */
    public static void sendInterfaceLog(InterfaceLog interfaceLog) {
        JSONObject paramJson = new JSONObject();
        JSONObject requestData = new JSONObject();
        requestData.put("destinationName", push_destinationName);
        paramJson.put("appName", push_appName);
        paramJson.put("appSecretKey", push_appSecretKey);
        String param = JSON.toJSONString(interfaceLog);
        paramJson.put("swiftNum", UUID.randomUUID());
        requestData.put("content", param);
        paramJson.put("requestData", requestData);
        //log.warn("MQ入参--{}",paramJson);
        try {
            sendFuture(paramJson.toString());
            log.info("pushLog mom request return : future");
        } catch (Exception e) {
            log.error("日志信息写入消息队列异常", e);
        }
    }

    private static void sendFuture(String msg) {
        BrokerLayerGrpc.BrokerLayerFutureStub brokerLayerFutureStub = GrpcClientInitConfig.grpcBroker();
        SendRequest sendrequest = SendRequest.newBuilder().setMsg(msg).build();
        brokerLayerFutureStub.sendMsg(sendrequest);
    }
}
