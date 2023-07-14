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
import com.br.marketing.entity.XieChengSmsCollidingReq;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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
    private static final  String  CODETYPE = "MOBILE";
    private static final  String  MARKETTYPE = "SMS";
    private static final  Boolean  MARKETFINANCEUSER = false;



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

    @Value("${api.xiecheng.smsColliding.openUrl:0}")
    private String smsCollidingOpenUrl;

    @Value("${api.xiecheng.smsColliding.appId:0}")
    private String smsCollidingAppId;

    @Value("${api.xiecheng.smsColliding.key:0}")
    private String smsCollidingKey;

    @Value("${api.xiecheng.smsColliding.iv:0}")
    private String smsCollidingIv;

    @Value("${api.xiecheng.smsColliding.singKey:0}")
    private String smsCollidingSingKey;

    @Value("${api.xiecheng.smsColliding.channel:0}")
    private String smsCollidingChannel;

    @Value("${api.xiecheng.smsQuit.isProxy:0}")
    private Boolean smsCollidingIsProxy;

    @Value("${api.xiecheng.smsCollidingVt.openUrl:0}")
    private String smsCollidingVtOpenUrl;

    @Value("${api.xiecheng.smsCollidingVt.appId:0}")
    private String smsCollidingVtAppId;

    @Value("${api.xiecheng.smsCollidingVt.key:0}")
    private String smsCollidingVtKey;

    @Value("${api.xiecheng.smsCollidingVt.iv:0}")
    private String smsCollidingVtIv;

    @Value("${api.xiecheng.smsCollidingVt.singKey:0}")
    private String smsCollidingVtSingKey;

    @Value("${api.xiecheng.smsCollidingVt.channel:0}")
    private String smsCollidingVtChannel;

    @Value("${api.xiecheng.smsCollidingVt.isProxy:0}")
    private Boolean smsCollidingVtIsProxy;


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
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(retMap, openUrl, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(thirdAdOuterReq),true,false);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("携程广告上报接口发送参数:ThirdAdOuterReq={} para={}", JSON.toJSONString(thirdAdOuterReq),JSON.toJSONString(retMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        String content = resMap.get("content");

//        /*范围40到70的随机数*/
//        int random = (int) (Math.random() * (70 - 40 + 1) + 40);
//        ThreadUtil.sleep(random);
//        String content = "{\"code\":0,\"msg\":\"测试效率\",\"data\":[{\"md5Code\":null,\"sha256Code\":\"760a06d2bc9b150d1d5b162e95bed32ed306cd1c2f7417c5e10397715ea165c1\",\"result\":false,\"orgChannel\":\"测试orgChannel\",\"mktLevel\":\"测试orgmktLevel\",\"info\":\"测试info\"}]}";

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

    /**
     * 短信碰撞接口
     * @param sha256CodeList
     * @return
     */
    @RetryMethod(retryNowNum = 3)
    public Result pushXieChengSmsCollidingData(List<String> sha256CodeList) {
        /**
         * data 组装
         */
        XieChengSmsCollidingReq xieChengSmsCollidingReq = new XieChengSmsCollidingReq(
                smsCollidingAppId,sha256CodeList,CODETYPE,MARKETTYPE,MARKETFINANCEUSER
        );
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", smsCollidingAppId);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", smsCollidingChannel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(xieChengSmsCollidingReq), smsCollidingKey, smsCollidingIv));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, smsCollidingSingKey));
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(retMap, smsCollidingOpenUrl, smsCollidingVtIsProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(xieChengSmsCollidingReq),true,false);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("携程短信撞库接口httpcode非200异常，重试");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        String content = resMap.get("content");
        //String content = "{\"code\":702,\"msg\":\"测试效率\",\"data\":[{\"md5Code\":null,\"sha256Code\":\"760a06d2bc9b150d1d5b162e95bed32ed306cd1c2f7417c5e10397715ea165c1\",\"result\":false,\"orgChannel\":\"测试orgChannel\",\"mktLevel\":\"测试orgmktLevel\",\"info\":\"测试info\"}]}";
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if(code==0){
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(content);
        }else {
            log.error("携程短信撞库接口请求返回code 非0异常，无重试，需要是手动处理。");
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(content);
        }

    }
    /**
     * 短信碰撞接口
     * @param sha256CodeList
     * @return
     */
    @RetryMethod(retryNowNum = 3)
    public Result pushXieChengSmsCollidingDataVt(List<String> sha256CodeList) {
        /**
         * data 组装
         */
        XieChengSmsCollidingReq xieChengSmsCollidingReq = new XieChengSmsCollidingReq(
                smsCollidingVtAppId,sha256CodeList,CODETYPE,MARKETTYPE,MARKETFINANCEUSER
        );
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", smsCollidingVtAppId);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", smsCollidingVtChannel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(xieChengSmsCollidingReq), smsCollidingVtKey, smsCollidingVtIv));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, smsCollidingVtSingKey));
//        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(retMap, smsCollidingVtOpenUrl, smsCollidingIsProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(xieChengSmsCollidingReq),true,false);
        HashMap<String, String> resMap = new HashMap<>();
//        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
//            log.error("携程短信撞库接口【新】httpcode非200异常，重试");
////            String content = "{\"msg\":\"非 200 网络异常\"}";
//            String content = "{\"code\":702,\"msg\":\"测试效率\",\"data\":[{\"md5Code\":null,\"sha256Code\":\"4674540929605c4e927054ef706eb3a744014891d463516f95ad33960317ceed\",\"result\":false,\"orgChannel\":\"测试orgChannel\",\"mktLevel\":\"测试orgmktLevel\",\"info\":\"测试info\"}]}";
//            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(content);
//        }
        String content = "{\"code\":0,\"msg\":\"测试效率\",\"data\":[{\"md5Code\":null,\"sha256Code\":\"4674540929605c4e927054ef706eb3a744014891d463516f95ad33960317ceed\",\"result\":false,\"orgChannel\":\"测试orgChannel\",\"mktLevel\":\"测试orgmktLevel\",\"info\":\"测试info\"}]}";
//        String content = resMap.get("content");

        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if(code==0){
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(content);
        }else {
            log.error("携程短信撞库接口请求【新】返回code 非0异常，无重试，需要是手动处理。");
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(content);
        }

    }
}
