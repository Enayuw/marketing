package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.bsf.ext.app.util.Ice1BSFConsumerBean;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.EncodeUtil;
import com.br.marketing.entity.RequestLog;
import com.br.mom.v3.broker_layer_api.api.BrokerLayerServicePrx;
import com.br.mom.v3.broker_layer_api.api.ResponseBean;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Created by Bairong on 2020/5/8.
 */
@Slf4j
public class MomUtil {
        public static void sendMom(String hxResult, JSONObject jsonData, RequestLog requestLog, String apiCode, String strategyId, String appSecretKey) {
            try {
                JSONObject jsonObject = JSONObject.parseObject(hxResult);
                if(jsonObject==null||jsonObject.isEmpty()){
                    return;
                }
                String swiftNumber = jsonObject.getString("swift_number").replace(Constants.PUBLIC_APICODE, apiCode);
                JSONObject requestStr = new JSONObject();
                requestStr.put("apiCode", apiCode);
                requestStr.put("strategyId", strategyId);
                requestStr.put("jsonData", jsonData);

                JSONObject reponseStr = new JSONObject();
                reponseStr.put("hxResult", jsonObject);
                reponseStr.put("swift_number", swiftNumber);
                reponseStr.put("code", jsonObject.getString("code"));
                reponseStr.put("message", "00".equals(jsonObject.getString("code")) ? "成功" : "");


                requestLog.setApiCode(apiCode);
                requestLog.setCode(jsonObject.getString("code"));
                requestLog.setSwiftNumber(swiftNumber);
                requestLog.setResponseStr(reponseStr.toString());
                requestLog.setRequestStr(EncodeUtil.encode(requestStr.toString(), "id,idCard,cell,name"));
                requestLog.setCostTime(requestLog.getResponseTime().getTime() - requestLog.getRequestTime().getTime());

                JSONObject paramJson = new JSONObject();
                JSONObject requestData = new JSONObject();
                requestData.put("destinationName", "marketing_query_log");
                paramJson.put("appName", "marketing_query_producer");
                paramJson.put("appSecretKey", appSecretKey);
                BrokerLayerServicePrx service = (BrokerLayerServicePrx) Ice1BSFConsumerBean.getServiceProxy(BrokerLayerServicePrx.class, "V3.0.0");
                service= (BrokerLayerServicePrx) service.ice_connectionCached(false);
                String param = JSON.toJSONString(requestLog);
                paramJson.put("swiftNum", UUID.randomUUID());
                requestData.put("content", param);
                paramJson.put("requestData", requestData);
                //log.info("MQ入参--{}",paramJson.toString());
                /*log.info("MQ入参--appName:{}--appSecretKey:{}--swiftNum:{}--destinationName:{}",paramJson.getString("appName"),
               paramJson.getString("appSecretKey"),paramJson.getString("swiftNum"),requestData.getString("destinationName"));*/
                try {
                    long l = System.currentTimeMillis();
                    ResponseBean sender = service.sender(paramJson.toString());
                    //AsyncResult asyncResult = service.begin_sender(paramJson.toString());
                      log.info("MQ耗时--{}--{}---{}",System.currentTimeMillis()-l,sender.getResult(),sender.getCode());

                } catch (Exception e) {
                    log.error("日志信息写入消息队列异常", e);
                }
            } catch (Exception e) {
                log.error("记录请求日志错误", e);
            }
    }
}
