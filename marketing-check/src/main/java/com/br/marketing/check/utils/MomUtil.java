package com.br.marketing.check.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.bsf.ext.app.util.Ice1BSFConsumerBean;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.common.utils.EncodeUtil;
import com.br.marketing.entity.RequestLog;
import com.br.mom.v3.broker_layer_api.api.BrokerLayerServicePrx;
import com.br.mom.v3.broker_layer_api.api.ResponseBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.Date;
import java.util.UUID;

/**
 * //				    _ooOoo_
 * //				   o8888888o
 * //				   88" . "88
 * //				   (| -_- |)
 * //				   O\  =  /O
 * //			    ____/`---'\____
 * //			  .'  \\|     |//  `.
 * //		     /  \\|||  :  |||//  \
 * //		    /  _|||||--:--|||||_  \
 * //		    | / | \\\  -  /// | \ |
 * //		    | \_|  ''\-:-/''  |_/ |
 * //		    \  .-\__  `-`  ___/-. /
 * //		  ___`...'  /--.--\  '...`___
 * //	   ."" '< `.___\_<|>_/___.'  >' "".
 * //	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 * //	    \ \ `-.  \_ __\ /__ _/  .-` / /
 * // ======`-.____`-.____\____/.-`____.-`======
 * //				    `=---='
 * //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //			  Buddha Bless, No Bug !
 *
 * @Author xiaoxin.pang
 * @Date 2020/6/1 13:13
 * @Description:
 **/
@Slf4j
public class MomUtil {

    public static void sendMom(String apiCode,String requestStr,String responseStr,Long costTime,String swiftNumber,String resultCode){
        RequestLog requestLog = new RequestLog();
        requestLog.setRequestTime(new Date());
        requestLog.setApiCode(apiCode);
        requestLog.setRequestStr(EncodeUtil.encodeDefault(requestStr));
        requestLog.setResponseStr(responseStr);
        requestLog.setResponseTime(new Date());
        requestLog.setCostTime(costTime);
        requestLog.setSwiftNumber(swiftNumber);
        requestLog.setUrl("push");
        requestLog.setCode(resultCode);
        MomUtil.sendMQ(requestLog);
    }



    /**
     * 发送mom
     * @param requestLog 请求日志
     */
    public static void sendMQ(final RequestLog requestLog){
        JSONObject paramJson=new JSONObject();
        JSONObject requestData=new JSONObject();
        /** 消息队列名称 */
        String destinationName = CkeckApplication.ac.getBean(Environment.class).getProperty("otherConfig.mom.destinationName");
        /** 生产者名称 */
        String appName = CkeckApplication.ac.getBean(Environment.class).getProperty("otherConfig.mom.appName");
        /** 消息队列密钥 */
        String appSecretKey = CkeckApplication.ac.getBean(Environment.class).getProperty("otherConfig.mom.appSecretKey");
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
        try {
             service.sender(paramJson.toString());
            //log.warn("MQ返回值--{}--{}",sender.getCode(),sender.getMessage());
        }catch (Exception e){
            log.error("日志信息写入消息队列异常",e);
        }
    }
}
