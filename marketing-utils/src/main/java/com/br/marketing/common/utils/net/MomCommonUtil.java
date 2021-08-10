package com.br.marketing.common.utils.net;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.bsf.ext.app.util.Ice1BSFConsumerBean;
import com.br.mom.v3.broker_layer_api.api.BrokerLayerServicePrx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class MomCommonUtil {

    @Value("otherConfig.mom.destinationName")
    private  String destinationName;

    @Value("otherConfig.mom.appName")
    private  String appName;

    @Value("otherConfig.mom.appSecretKey")
    private  String appSecretKey;

    /**
     * 发送mom
     * @param requestLog 请求日志
     */
    public void sendMQ(final InterfaceLog requestLog){
        try {
        JSONObject paramJson=new JSONObject();
        JSONObject requestData=new JSONObject();
        requestData.put("destinationName",destinationName);
        paramJson.put("appName",appName);
        paramJson.put("appSecretKey",appSecretKey);
        BrokerLayerServicePrx service = (BrokerLayerServicePrx) Ice1BSFConsumerBean.getServiceProxy(BrokerLayerServicePrx.class, "V3.0.0");
        service = (BrokerLayerServicePrx) service.ice_connectionCached(false);
        String param = JSON.toJSONString(requestLog);
        paramJson.put("swiftNum", UUID.randomUUID());
        requestData.put("content",param);
        paramJson.put("requestData",requestData);
        //log.warn("MQ入参--{}",paramJson);
            service.sender(paramJson.toString());
            //log.warn("MQ返回值--{}--{}",sender.getCode(),sender.getMessage());
        }catch (Exception e){
            log.error("日志信息写入消息队列异常",e);
        }
    }
}
