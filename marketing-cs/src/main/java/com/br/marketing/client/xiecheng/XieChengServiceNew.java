package com.br.marketing.client.xiecheng;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengSmsCollidingReq;
import com.br.marketing.service.Impl.xc.XcExceptionDataRetryService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 携程处理
 * <p>
 * --------------------------------
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

    @Autowired
    HttpProxyClient httpProxyClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    private RedisChgService redisChgService;

    @Resource
    XcExceptionDataRetryService retryService;

    private HashMap<String, String> getTestMap(List<String> sha256CodeList) {
        JSONObject map = new JSONObject();
        if (marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(2)) {
            map.put("code", 707);
            map.put("msg", "测试挡板非0异常");
        } else {
            map.put("code", 0);
            map.put("msg", "success");
        }

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < sha256CodeList.size(); i++) {
            JSONObject dataMap = new JSONObject();
            String s = sha256CodeList.get(i);
            dataMap.put("sha256Code", s);
            if (i % 2 == 0) {
                dataMap.put("result", true);
                dataMap.put("releaseTime", DateUtil.formatDateTime(DateUtil.offsetDay(new Date(), 7)));
            } else {
                dataMap.put("result", false);
            }
            dataMap.put("orgChannel", "测试orgChannel");
            dataMap.put("mktLevel", "测试mktLevel");
            dataMap.put("info", "测试info");
            jsonArray.add(dataMap);
        }
        map.put("data", jsonArray);
        HashMap<String, String> resMap = new HashMap<>();
        if (marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(1)) {
            resMap.put("httpcode", "201");
        } else {
            resMap.put("httpcode", "200");
        }

        resMap.put("content", map.toString());
        return resMap;

    }

    /**
     * 携程撞库方法
     * 使用范围：TRUE数据撞库、FALSE数据撞库、异常数据重试撞库
     * @param sha256CodeList
     * @return
     */
    public Result pushXieChengSmsCollidingDataNew(List<String> sha256CodeList) {
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
        if (marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(0)) {
            resMap = getTestMap(sha256CodeList);
        } else {
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
                shutDownConditionSwitchAndAlert("携程撞库暂停通知:code返回707");
            }

            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }

    }

    /**
     * 关闭条件开关并发送钉钉告警
     * @param msg
     */
    public void shutDownConditionSwitchAndAlert(String msg) {
        // 关闭条件开关
        shutDownConditionSwitch();
        // 钉钉告警
        retryService.sendDingDingAlert("携程撞库暂停通知", msg);
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
        int secondsUntilEndOfDay = (int) ChronoUnit.SECONDS.between(now, endOfDay);
        try {
            redisChgService.setex(RedisKeyConstant.XIECHENG_CONDITIONSWITCH, "false", secondsUntilEndOfDay);
        } catch (Exception e) {
            log.error("携程数据撞库，关闭redis条件开关失败:" + e.getMessage(), e);
        }
    }
}