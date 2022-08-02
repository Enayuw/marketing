package com.br.marketing.client.xiecheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.entity.ThirdAdOuterReq;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 携程处理
 * <p>
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.client.xiecheng
 * @Description: 携程处理
 * @CreateTime: 2022-07-19 15 :01
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@Slf4j
@Service
public class XieChengService {


    //xiecheng:
    //openUrl: https://cgcallback-fat.ctripqa.com/nemoweb/ad/common/outAdMonitor.do
    //appId: bairong001
    //key: f3df6f62f0527bf0
    //iv: 3b2dac323465b024
    //singKey: 95cc01ec07387a44
    //source: BaiRong_C01
    //channel: commonOutAdMonitor

    @Value("${api.xiecheng.openUrl:0}")
    private String openUrl;

    @Value("${api.xiecheng.appId:0}")
    private String appId;

    @Value("${api.xiecheng.key:0}")
    private String key;

    @Value("${api.xiecheng.iv:0}")
    private String iv;

    @Value("${api.xiecheng.singKey:0}")
    private String singKey;

    @Value("${api.xiecheng.source:0}")
    private String source;

    @Value("${api.xiecheng.channel:0}")
    private String channel;

    @Value("${api.xiecheng.isProxy:0}")
    private Boolean isProxy;


    @Autowired
    HttpProxyClient httpProxyClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;


    @RetryMethod(retryNowNum = 3)
    public String pushXieChengData(XieChengData xieChengData) {
        log.warn("携程明文参数 para={}", xieChengData);
        try {
            return send(xieChengData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String send(XieChengData xieChengData) throws Exception {
        /**
         * data 组装
         */
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("sha256Tel", xieChengData.getClickTel());
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        ThirdAdOuterReq thirdAdOuterReq = new ThirdAdOuterReq(
                timestemp,
                source,
                xieChengData.getClickId(),
                xieChengData.getActionType(),
                deviceInfo.toString()
        );
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", appId);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", channel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(thirdAdOuterReq), key, iv));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, singKey));
        log.warn("携程发送参数 para={}", JSON.toJSONString(retMap));
        String send = httpProxyClient.send(JSON.toJSONString(retMap), openUrl, isProxy);
        log.warn("携程数据返回信息：{}", send);
        return send;

    }

}
