package com.br.marketing.client.xiecheng;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.counter.BrCounter;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.xiecheng.intput.AdReqDTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.ThirdAdOuterReq;
import com.br.marketing.entity.XieChengSmsCollidingReq;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private static final String CODETYPE = "MOBILE";
    private static final String MARKETTYPE = "SMS";
    private static final Boolean MARKETFINANCEUSER = false;


    @Value("${api.xiecheng.openUrl:0}")
    private String openUrl;

    @Value("${api.xiecheng.appId:0}")
    private String appId;

    @Value("${api.xiecheng.appIdVt:0}")
    private String appIdVt;

    @Value("${api.xiecheng.key:0}")
    private String key;

    @Value("${api.xiecheng.keyVt:0}")
    private String keyVt;

    @Value("${api.xiecheng.iv:0}")
    private String iv;

    @Value("${api.xiecheng.ivVt:0}")
    private String ivVt;

    @Value("${api.xiecheng.singKey:0}")
    private String singKey;

    @Value("${api.xiecheng.signKeyVt:0}")
    private String signKeyVt;

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


    @Value("${api.xiecheng.smsColliding.appIdVt:0}")
    private String smsCollidingVtAppId;

    @Value("${api.xiecheng.smsColliding.keyVt:0}")
    private String smsCollidingVtKey;

    @Value("${api.xiecheng.smsColliding.ivVt:0}")
    private String smsCollidingVtIv;

    @Value("${api.xiecheng.smsColliding.signKeyVt:0}")
    private String smsCollidingVtSignKey;




    @Autowired
    HttpProxyClient httpProxyClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    private static final String XIECHENGSMSQUIT = "xieChengSmsQuit";


    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result pushXieChengData(AdReqDTO xieChengData) {

        /**
         * data 组装
         */
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("sha256Tel", xieChengData.getSha256Tel());
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        ThirdAdOuterReq thirdAdOuterReq = null;
        JSONObject config = marketingCommonConfig.getXieChengVtConfig().get("adVt");
        String aid = "1".equals(xieChengData.getConditionKey()) ? appId : config.getString("appId");
        String aesKey = "1".equals(xieChengData.getConditionKey()) ? key : config.getString("aesKey");
        String ivKey = "1".equals(xieChengData.getConditionKey()) ? iv : config.getString("iv");
        String sKey = "1".equals(xieChengData.getConditionKey()) ? singKey : config.getString("singKey");
        String sourceVt = config.getString("source");
        if ("1".equals(xieChengData.getConditionKey())) {
            String extendSource = source;
            try {
                JSONObject extend = JSONObject.parseObject(xieChengData.getExtend());
                String sourceStr = extend.getString("source");
                if (StringUtils.isEmpty(sourceStr)) {
                    log.warn("携程广告上报接口，source为空:{}，置为默认值:{}", sourceStr, source);
                } else {
                    extendSource = sourceStr;
                }
            } catch (Exception e) {
                log.error("携程广告上报接口，source字段解析异常:{}", xieChengData.getExtend(), e);
            }

            thirdAdOuterReq = new ThirdAdOuterReq(
                    timestemp,
                    extendSource,
                    xieChengData.getClickId(),
                    xieChengData.getActionType(),
                    deviceInfo.toString()
            );
        } else {

            thirdAdOuterReq = new ThirdAdOuterReq(
                    timestemp,
                    sourceVt,
                    xieChengData.getClickId(),
                    xieChengData.getActionType(),
                    deviceInfo.toString(),
                    xieChengData.getMktMode(),
                    xieChengData.getMktChannel(),
                    xieChengData.getMktProductNo(),
                    aid
            );
        }



        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", aid);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", channel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(thirdAdOuterReq), aesKey , ivKey));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, sKey));
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(retMap, openUrl, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(thirdAdOuterReq), true, false);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("携程广告上报接口发送参数:ThirdAdOuterReq={} para={}", JSON.toJSONString(thirdAdOuterReq), JSON.toJSONString(retMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        String content = resMap.get("content");

        /*范围40到70的随机数*/
//        int random = (int) (Math.random() * (70 - 40 + 1) + 40);
//        ThreadUtil.sleep(70);
//        String content = "{\"code\":0,\"msg\":\"测试效率\",\"data\":[{\"md5Code\":null,\"sha256Code\":\"760a06d2bc9b150d1d5b162e95bed32ed306cd1c2f7417c5e10397715ea165c1\",\"result\":false,\"orgChannel\":\"测试orgChannel\",\"mktLevel\":\"测试orgmktLevel\",\"info\":\"测试info\"}]}";

        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if (code == 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(content);
        }
        if (code == 500 || code == 704) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(content);
        } else {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(content);
        }

    }

    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result pushXieChengDataNew(AdReqDTO xieChengData) {
        return pushXieChengDataNew(xieChengData, false);
    }

    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result pushXieChengDataNew(AdReqDTO xieChengData, boolean mock) {
        log.warn("携程上报新接口逻辑："+xieChengData.getSha256Tel());
        // 1. 获取配置
        Map<String, JSONObject> configMap = marketingCommonConfig.getXieChengCpaAndCpsConfig();
        JSONObject config = "1".equals(xieChengData.getConditionKey()) ? configMap.get("cpa") : configMap.get("cps");

        // 2. 准备基础数据
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("sha256Tel", xieChengData.getSha256Tel());

        JSONObject mktProductNoConfig = config.getJSONObject("mktProductNoConfig");
        boolean mktProductNoConfigExist = StringUtils.isNotEmpty(mktProductNoConfig);
        JSONObject extend = JSONObject.parseObject(xieChengData.getExtend());

        // 3. 获取source
        String source;
        String extendSource = extend.getString("source");
        if (mktProductNoConfigExist) {
            if (mktProductNoConfig.keySet().contains(extendSource)) {
                source = extendSource;
            } else {
                source = config.getString("source");
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                        "携程广告上报接口，id=" + xieChengData.getId() +
                                "的xieChengData的extend中source未配置，已由" + extendSource + "调整为" + source));
            }
        } else {
            if (StringUtils.isNotEmpty(extendSource)) {
                source = extendSource;
            } else {
                source = config.getString("source");
                log.warn("携程广告上报接口，id:{}的xieChengData的extend中source字段为空，置为默认值:{}", xieChengData.getId() , source);
            }
        }

        // 4. 获取mktProductNo
        String mktProductNo;
        String extendMktProductNo = extend.getString("mktProductNo");
        if (mktProductNoConfigExist) {
            mktProductNo = mktProductNoConfig.getString(source);
            if(!Objects.equals(extendMktProductNo, mktProductNo)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                        "携程广告上报接口，id=" + xieChengData.getId() +
                                "的xieChengData的extend中mktProductNo已由" + extendMktProductNo + "调整为" + mktProductNo));
            }
        } else {
            if (StringUtils.isNotEmpty(extendMktProductNo)) {
                mktProductNo = extendMktProductNo;
            } else {
                mktProductNo = config.getString("mktProductNo");
                log.warn("携程广告上报接口，id:{}的xieChengData的extend中mktProductNo字段为空，置为默认值:{}", xieChengData.getId() , mktProductNo);
            }
        }

        if(mock) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("mock success");
        }

        // 4. 构建请求对象
        ThirdAdOuterReq thirdAdOuterReq = new ThirdAdOuterReq(
                timestamp,
                source,
                xieChengData.getClickId(),
                xieChengData.getActionType(),
                deviceInfo.toString(),
                config.getString("mktMode"),
                xieChengData.getMktChannel(),
                mktProductNo,
                config.getString("appId")
        );

        // 5. 准备请求参数
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("appId", config.getString("appId"));
        retMap.put("timestamp", timestamp);
        retMap.put("channel", config.getString("channel"));
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(thirdAdOuterReq),
                config.getString("aesKey"), config.getString("iv")));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, config.getString("singKey")));

        // 6. 发送请求
        Map<String, String> resMap = httpProxyClient.sendByCodeWithLog(
                retMap,
                config.getString("url"),
                config.getBoolean("isProxy"),
                MediaType.APPLICATION_JSON_UTF8_VALUE,
                JSON.toJSONString(thirdAdOuterReq),
                true,
                false
        );

        // 7. 处理响应
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                    , "携程上报异常重试3次！ThirdAdOuterReq="+JSON.toJSONString(thirdAdOuterReq)));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        JSONObject resultJson = JSONObject.parseObject(resMap.get("content"));
        Integer code = resultJson.getInteger("code");

        // 8. 根据响应码返回结果
        if (code == 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue())
                    .setMessage(resMap.get("content"));
        }
        return new Result()
                .setCode(code == 500 || code == 704 ? ResultCode.INTERNAL_SERVER_ERROR.getValue() : ResultCode.FAIL.getValue())
                .setMessage(resMap.get("content"));
    }

    /**
     * desc：携程短信退订接口
     */
    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result sendSmsQuitData(SmsQuitReq smsQuitReq) {
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String,String> config = marketingCommonConfig.getXieChengSmsQuitConfig().get(smsQuitReq.getApiCode());
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", Objects.isNull(config.get("appId"))?smsQuitAppId: config.get("appId"));
        retMap.put("timestamp", timestemp);
        retMap.put("channel", smsQuitChannel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(smsQuitReq),
                Objects.isNull(config.get("aesKey"))?smsQuitKey: config.get("aesKey"),
                Objects.isNull(config.get("aesIv"))?smsQuitIv: config.get("aesIv")));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, Objects.isNull(config.get("signKey"))?smsQuitSingKey: config.get("signKey")));
        List<Boolean> logStore = httpProxyClient.isLogStore(XIECHENGSMSQUIT);
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(retMap, smsQuitOpenUrl, smsQuitIsProxy,
                MediaType.APPLICATION_JSON_UTF8_VALUE, "", logStore.get(0), logStore.get(1));
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("携程短信退订接口-请求参数:{};返回:{}", JSON.toJSONString(resMap), JSON.toJSONString(resMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        JSONObject resultJson = JSONObject.parseObject(resMap.get("content"));
        Integer code = resultJson.getInteger("code");
        if (code == 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        //需要重试
        if (code == 500 || code == 704) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        } else {
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
    }

    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result sendSmsQuitDataNew(SmsQuitReq smsQuitReq) {
        log.warn("短信退订新地址调用，smsQuitReq：" + JSON.toJSONString(smsQuitReq));
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String,String> config = marketingCommonConfig.getXieChengSmsQuitConfigNew().get(smsQuitReq.getApiCode());
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", Objects.isNull(config.get("appId")) ? smsQuitAppId : config.get("appId"));
        retMap.put("timestamp", timestemp);
        retMap.put("channel", Objects.isNull(config.get("channel")) ? channel : config.get("channel"));
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(smsQuitReq),
                Objects.isNull(config.get("aesKey")) ? smsQuitKey : config.get("aesKey"),
                Objects.isNull(config.get("aesIv")) ? smsQuitIv: config.get("aesIv")));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, Objects.isNull(config.get("signKey")) ? smsQuitSingKey : config.get("signKey")));
        List<Boolean> logStore = httpProxyClient.isLogStore(XIECHENGSMSQUIT);
        String url = Objects.isNull(config.get("url")) ? smsQuitOpenUrl : config.get("url");
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(retMap, url, smsQuitIsProxy,
                MediaType.APPLICATION_JSON_UTF8_VALUE, "", logStore.get(0), logStore.get(1));
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("携程短信退订接口-请求参数:{};返回:{}", JSON.toJSONString(resMap), JSON.toJSONString(resMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        JSONObject resultJson = JSONObject.parseObject(resMap.get("content"));
        Integer code = resultJson.getInteger("code");
        if (code == 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        //需要重试
        if (code == 500 || code == 704) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        } else {
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
    }

    /**
     * 短信碰撞接口
     *
     * @param sha256CodeList
     * @return
     */
    @RetryMethod(retryNowNum = 3)
    public Result pushXieChengSmsCollidingData(List<String> sha256CodeList) {
        /**
         * data 组装
         */
        XieChengSmsCollidingReq xieChengSmsCollidingReq = new XieChengSmsCollidingReq(
                smsCollidingAppId, sha256CodeList, CODETYPE, MARKETTYPE, MARKETFINANCEUSER
        );
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", smsCollidingAppId);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", smsCollidingChannel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(xieChengSmsCollidingReq), smsCollidingKey, smsCollidingIv));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, smsCollidingSingKey));
        HashMap<String, String> resMap = new HashMap<>();
        if(marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(0)){
         resMap = getTestMap(sha256CodeList);
        }else {
          resMap = httpProxyClient.sendByCodeWithLog(retMap, smsCollidingOpenUrl, smsCollidingIsProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(xieChengSmsCollidingReq), true, false);
        }
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("携程短信撞库接口httpcode非200异常");
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("{'msg':'httpCode非200'}");
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if (code == 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(content);
        } else {
            log.error("携程短信撞库接口请求返回code 非0异常");
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(content);
        }

    }

    /**
     * 黑名单撞库接口
     *
     * @param sha256CodeList
     * @return
     */
    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<String> pushXieChengSmsCollidingDataVt(List<String> sha256CodeList) {
        /*
          data 组装
         */
        JSONObject collidingConfig = marketingCommonConfig.getXieChengSmsCollidingConfigVt();

        String smsCollidingOpenUrl = collidingConfig.getString("smsCollidingOpenUrl");
        String smsCollidingAppId = collidingConfig.getString("smsCollidingAppId");
        String smsCollidingKey = collidingConfig.getString("smsCollidingKey");
        String smsCollidingIv = collidingConfig.getString("smsCollidingIv");
        String smsCollidingSingKey = collidingConfig.getString("smsCollidingSingKey");
        String smsCollidingChannel = collidingConfig.getString("smsCollidingChannel");
        Boolean smsCollidingIsProxy = collidingConfig.getBoolean("smsCollidingIsProxy");
        XieChengSmsCollidingReq xieChengSmsCollidingReq = new XieChengSmsCollidingReq(
                smsCollidingAppId, sha256CodeList, CODETYPE, MARKETTYPE, MARKETFINANCEUSER
        );

        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", smsCollidingAppId);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", smsCollidingChannel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(xieChengSmsCollidingReq), smsCollidingKey, smsCollidingIv));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, smsCollidingSingKey));
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(retMap, smsCollidingOpenUrl, smsCollidingIsProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(xieChengSmsCollidingReq),true,false);
//        HashMap<String, String> resMap = getTestMap(sha256CodeList);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            String content = " {\"code\":702,\"msg\":\"网络异常或者返回内容为空\"}";
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(content);
        }
        log.warn("vt撞库参数 appId:"+smsCollidingAppId
                +",smsCollidingIv:"+smsCollidingIv
                +",smsCollidingKey" +smsCollidingKey
                +"smsCollidingOpenUrl:"+smsCollidingOpenUrl
        +",smsCollidingSingKey:"+smsCollidingSingKey);
        String content = resMap.get("content");
        //String content = "{\"code\":702,\"msg\":\"测试效率\",\"data\":[{\"md5Code\":null,\"sha256Code\":\"760a06d2bc9b150d1d5b162e95bed32ed306cd1c2f7417c5e10397715ea165c1\",\"result\":false,\"orgChannel\":\"测试orgChannel\",\"mktLevel\":\"测试orgmktLevel\",\"info\":\"测试info\"}]}";
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if (code == 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(content);
        } else {
            log.error("携程短信撞库接口请求【新】返回code 非0异常，无重试，需要是手动处理。");
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(content);
        }

    }

    /**
     * cps撞库接口
     *
     * @param sha256CodeList
     * @return
     */
    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<String> pushXieChengCpsCollidingData(List<String> sha256CodeList) {
        JSONObject collidingConfig = marketingCommonConfig.getXieChengCpsCollidingConfig();
        String smsCollidingOpenUrl = collidingConfig.getString("smsCollidingOpenUrl");
        String smsCollidingAppId = collidingConfig.getString("smsCollidingAppId");
        String smsCollidingKey = collidingConfig.getString("smsCollidingKey");
        String smsCollidingIv = collidingConfig.getString("smsCollidingIv");
        String smsCollidingSingKey = collidingConfig.getString("smsCollidingSingKey");
        String smsCollidingChannel = collidingConfig.getString("smsCollidingChannel");
        Boolean smsCollidingIsProxy = collidingConfig.getBoolean("smsCollidingIsProxy");
        /**
         * data 组装
         */
        XieChengSmsCollidingReq xieChengSmsCollidingReq = new XieChengSmsCollidingReq(
                smsCollidingAppId, sha256CodeList, CODETYPE, MARKETTYPE, MARKETFINANCEUSER
        );
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", smsCollidingAppId);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", smsCollidingChannel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(xieChengSmsCollidingReq), smsCollidingKey, smsCollidingIv));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, smsCollidingSingKey));
        HashMap<String, String> resMap;
        if(marketingCommonConfig.getXieChengCpsCollidingRetrySwitch().get(0)){
            resMap = getCpsTestMap(sha256CodeList);
        }else {
            resMap = httpProxyClient.sendByCodeWithLog(retMap, smsCollidingOpenUrl, smsCollidingIsProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(xieChengSmsCollidingReq), true, false);
        }
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");

        try {
            //调用数量监控
            BrCounter.count(PrometheusMonitorUtils.COUNT_XIECHENG_CPS_COLLIDING_DATA_METRIC_NAME, "3710090", "xc-cps",
                    sha256CodeList.size());
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.XIECHENG_INTERFACEERROR.getCode(), "携程CPS记录撞库量级异常！"), ex);
        }

        if (code == 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(JSON.toJSONString(resMap));
        } else {
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }
    }

    private HashMap<String,String> getTestMap(List<String> sha256CodeList){
        JSONObject map = new JSONObject();
        if(marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(2)){
            map.put("code",9999);
            map.put("msg","测试挡板非0异常");
        }else {
            map.put("code",0);
            map.put("msg","success");
        }

        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<sha256CodeList.size();i++){
            JSONObject dataMap = new JSONObject();
            String s = sha256CodeList.get(i);
            dataMap.put("sha256Code",s);
            if(i%2==0){
                dataMap.put("result",true);
                dataMap.put("releaseTime", DateUtil.formatDateTime(DateUtil.offsetDay(new Date(),7)));
            }else {
                dataMap.put("result",false);
            }
            dataMap.put("md5Code",null);
            dataMap.put("releaseDate",null);
            dataMap.put("hitRequestNo", RandomUtil.randomString(29).toUpperCase());
            dataMap.put("orgChannel","测试orgChannel");
            dataMap.put("mktLevel","测试mktLevel");
            dataMap.put("info","测试info");
            jsonArray.add(dataMap);
        }
        map.put("data",jsonArray);
        HashMap<String, String> resMap = new HashMap<>();
        if(marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(1)){
            resMap.put("httpcode","201");
        }else {
            resMap.put("httpcode","200");
        }

        resMap.put("content",map.toString());
        return resMap;

    }
    private HashMap<String,String> getCpsTestMap(List<String> sha256CodeList){
        JSONObject map = new JSONObject();
        if(marketingCommonConfig.getXieChengCpsCollidingRetrySwitch().get(2)){
            map.put("code",9999);
            map.put("msg","测试挡板非0异常");
        }else {
            map.put("code",0);
            map.put("msg","success");
        }

        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<sha256CodeList.size();i++){
            JSONObject dataMap = new JSONObject();
            String s = sha256CodeList.get(i);
            dataMap.put("sha256Code",s);
            if(i%2==0){
                dataMap.put("result",true);
                dataMap.put("releaseTime", DateUtil.formatDateTime(DateUtil.offsetDay(new Date(),7)));
            }else {
                dataMap.put("result",false);
            }
            dataMap.put("md5Code",null);
            dataMap.put("releaseDate",null);
            dataMap.put("hitRequestNo", RandomUtil.randomString(29).toUpperCase());
            dataMap.put("orgChannel","测试orgChannel");
            dataMap.put("mktLevel","测试mktLevel");
            dataMap.put("info","测试info");
            jsonArray.add(dataMap);
        }
        map.put("data",jsonArray);
        HashMap<String, String> resMap = new HashMap<>();
        if(marketingCommonConfig.getXieChengCpsCollidingRetrySwitch().get(1)){
            resMap.put("httpcode","201");
        }else {
            resMap.put("httpcode","200");
        }

        resMap.put("content",map.toString());
        return resMap;

    }
}

