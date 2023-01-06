package com.br.marketing.client.xiecheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.ThirdAdOuterReq;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

    @Value("${api.xiecheng.smsQuit.openUrl:0}")
    private String smsQuitOpenUrl;

    @Value("${api.xiecheng.smsQuit.appId:0}")
    private String smsQuitAppId;

    @Value("${api.xiecheng.smsQuit.key:0}")
    private String smsQuitKey;

    @Value("${api.xiecheng.smsQuit.iv:0}")
    private String smsQuitIv;

    @Value("${api.xiecheng.smsQuit.singKey:0}")
    private String smsQuitSingKey;

    @Value("${api.xiecheng.smsQuit.channel:0}")
    private String smsQuitChannel;

    @Value("${api.xiecheng.smsQuit.isProxy:0}")
    private Boolean smsQuitIsProxy;


    @Autowired
    HttpProxyClient httpProxyClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    private static final  String XIECHENGSMSQUIT= "xieChengSmsQuit";


    @RetryMethod(retryNowNum = 3)
    public Result pushXieChengData(XieChengData xieChengData) {
        /**
         * data 组装
         */
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("sha256Tel", xieChengData.getSha256Tel());
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
        //
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(retMap, openUrl, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(thirdAdOuterReq),true,false);
//        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
//            log.error("携程广告上报接口发送参数:ThirdAdOuterReq={} para={}", JSON.toJSONString(thirdAdOuterReq),JSON.toJSONString(retMap));
//            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
//        }
//        String content = resMap.get("content");
        String content = "{\"code\":0,\"msg\":\"测试成功\",\"data\":null}";
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if(code==0){
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(content);
        }
        if (code == 500 || code == 704) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(content);
        }else {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(content);
        }

    }

    /**
     * desc：携程短信退订接口
     */
    @RetryMethod(retryNowNum = 3)
    public Result sendSmsQuitData(SmsQuitReq smsQuitReq) {
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", smsQuitAppId);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", smsQuitChannel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(smsQuitReq), smsQuitKey, smsQuitIv));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, smsQuitSingKey));
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(retMap, smsQuitOpenUrl, smsQuitIsProxy, MediaType.APPLICATION_JSON_UTF8_VALUE,"", httpProxyClient.isLogStore(XIECHENGSMSQUIT).get(0), httpProxyClient.isLogStore(XIECHENGSMSQUIT).get(1));
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("携程短信退订接口-请求参数:{};返回:{}",JSON.toJSONString(resMap),JSON.toJSONString(resMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        JSONObject resultJson = JSONObject.parseObject(resMap.get("content"));
        Integer code = resultJson.getInteger("code");
        if(code==0){
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        //需要重试
        if(code==500||code==704){
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }else{
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
    }

}
