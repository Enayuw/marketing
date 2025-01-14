package com.br.marketing.client.xiecheng;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengSmsCollidingReq;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
public class XieChengServiceNew {
    private static final String CODETYPE = "MOBILE";
    private static final String MARKETTYPE = "SMS";
    private static final Boolean MARKETFINANCEUSER = false;

    @Autowired
    HttpProxyClient httpProxyClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    private RedisChgService redisChgService;

    private HashMap<String,String> getTestMap(List<String> sha256CodeList){
        JSONObject map = new JSONObject();
        if(marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(2)){
            map.put("code",707);
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
                dataMap.put("releaseDate", null);
                dataMap.put("hitRequestNo", RandomUtil.randomString(29).toUpperCase());
                Random random = new Random();
                int randomNumber = random.nextInt(5);
                if (randomNumber == 0) {
                    dataMap.put("info", "退订用户，不可短信营销");
                } else if (randomNumber == 1) {
                    dataMap.put("info", null);
                } else if (randomNumber == 2) {

                } else {
                    dataMap.put("info","测试info");
                }
            }else {
                dataMap.put("result",false);
                dataMap.put("hitRequestNo", null);
                dataMap.put("releaseDate", DateUtil.formatDate(DateUtil.offsetDay(new Date(), RandomUtil.getRandom().nextInt(7)+1)));
                dataMap.put("info","测试info");
            }
            dataMap.put("orgChannel","测试orgChannel");
            dataMap.put("mktLevel","测试mktLevel");
            if (marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(3)) {
                switch (i % 3) {
                    case 0:
                        dataMap.put("marketCouponList", JSONArray.parseArray("[{\"couponCode\":\"券码Code\",\"couponDesc\":\"券码描述\"}]"));
                        break;
                    case 1:
                        dataMap.put("marketCouponList", JSONArray.parseArray(
                            "[{\"couponCode\":\"券码Code1\",\"couponDesc\":\"券码描述1\"},{\"couponCode\":\"券码Code2\",\"couponDesc\":\"券码描述2\"}]"));
                        break;
                    case 2:
                        dataMap.put("marketCouponList", "测试非规定marketCouponList格式，不影响撞库，只是不析出marketCouponList！");
                        break;
                    default:
                        break;
                }
            }
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

    /**
     * 携程撞库方法
     * 使用范围：TRUE数据撞库、FALSE数据撞库、异常数据重试撞库
     *
     * @param sha256CodeList
     * @return
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d,0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result pushXieChengSmsCollidingDataNew(List<String> sha256CodeList) {
        JSONObject collidingConfig = marketingCommonConfig.getXieChengSmsCollidingConfig();
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
        if(marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(0)){
            resMap = getTestMap(sha256CodeList);
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
        if (code == 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(JSON.toJSONString(resMap));
        } else {
            if (code == 707) {
                shutDownConditionSwitch();
            }

            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }
    }

    /**
     * 携程撞库一次性初始化流水号方法
     * 使用范围：一次性初始化流失号作业
     *
     * @param sha256CodeList
     * @return
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d,0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result pushXieChengForInitHitRequestNo(List<String> sha256CodeList) {
        JSONObject collidingConfig = marketingCommonConfig.getXieChengCollidingInitRequestNoConfig();
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
        if(marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(0)){
            resMap = getTestMap(sha256CodeList);
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
        if (code == 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(JSON.toJSONString(resMap));
        } else {
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }
    }

    /**
     * 关闭条件开关，直至当天23:59:59
     */
    public void shutDownConditionSwitch() {
        // 当前日期
        LocalDateTime now = LocalDateTime.now();
        // 当前时间至23:59:59
        LocalDateTime endOfDay = now.with(LocalTime.MAX);
        // 计算当前时间至23:59:59的秒数
        int secondsUntilEndOfDay = (int)ChronoUnit.SECONDS.between(now, endOfDay);
        try {
            redisChgService.setex(RedisKeyConstant.XIECHENG_CONDITIONSWITCH, "false",secondsUntilEndOfDay);
        } catch (Exception e) {
            log.error("携程数据撞库，关闭redis条件开关失败:" + e.getMessage(), e);
        }
    }
}