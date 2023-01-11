package com.br.marketing.check.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.xiecheng.FinanceAESUtils;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
import com.br.marketing.entity.ThirdAdOuterReq;
import com.br.marketing.entity.XieChengSmsCollidingReq;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.service.MarketingSmyPushService;
import com.br.marketing.service.PushDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.check.controller
 * @Description:
 * @CreateTime: 2022-07-18 17 :09
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@RestController
@RequestMapping("/test/")
@Slf4j
public class TestXiechengController {


    @Autowired
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;
    @Resource
    MarketingSmyPushService marketingSmyPushService;


    @Autowired
    private HttpProxyClient httpProxyClient;
    @Autowired
    PushDataService pushDataService;

    @GetMapping("/test")
    public void transfersmyTest(String id) {
        pushDataService.pushXieChengToDbData(id);
    }

    @GetMapping("/resultVolumeCheck")
    public void process() {
        PhoneSaleExtendInfoExample updateExample = new PhoneSaleExtendInfoExample();
        List<Long> ids = new ArrayList<>();
        ids.add(690057L);
        updateExample.createCriteria().andIdIn(ids);
        PhoneSaleExtendInfo updateEntity = new PhoneSaleExtendInfo();
        updateEntity.setPStatus(2);
        phoneSaleExtendInfoMapper.updateByExampleSelective(updateEntity, updateExample);
    }

    @GetMapping("testSms")
    public void test() throws JSONException {
        /**
         * data 组装
         * 域名：http://cgcallback-fat.ctripqa.com/nemoweb/ad/common/unionCheckUser.do
         *
         * channel：commonUnionCheckUser
         * appId：bairong001
         * signKey：95cc01ec07387a44
         * aesKey：f3df6f62f0527bf0
         * aesIv：3b2dac323465b024
         */

        String url = "https://cgcallback-fat.ctripqa.com/nemoweb/ad/common/unionCheckUser.do";

        String appId = "bairong001";
        String key = "f3df6f62f0527bf0";
        String iv = "3b2dac323465b024";
        String singKey = "95cc01ec07387a44";

        String codeType = "MOBILE";
        String marketType = "SMS";
        Boolean marketFinanceUser = false;
        List<String> sha256CodeLists = new ArrayList<>();
        sha256CodeLists.add("aeaa638c17d05884717153c3c898a41598e87209ac42e4cfcd844f6e724c33d7");

        String channel = "commonUnionCheckUser";

        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        XieChengSmsCollidingReq xieChengSmsCollidingReq = new XieChengSmsCollidingReq(
                appId, sha256CodeLists, codeType, marketType, marketFinanceUser
        );
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", appId);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", channel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(xieChengSmsCollidingReq), key, iv));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, singKey));
        System.out.println(retMap);
        String send = "{\"code\":0,\"msg\":\"成功\",\"data\":[{\"md5Code\":null,\"sha256Code\":\"aeaa638c17d05884717153c3c898a41598e87209ac42e4cfcd844f6e724c33d7\",\"result\":false,\"orgChannel\":null,\"mktLevel\":null,\"info\":null}]}";

        //https://cgcallback-fat.ctripqa.com/nemoweb/ad/common/outAdMonitor.do
        System.out.println(JSON.toJSONString(send));
    }

    public static String getTimeDay(String simpleDateFormat, int index) {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat(simpleDateFormat);
        calendar.add(Calendar.DAY_OF_MONTH, index);
        String date = fmt.format(calendar.getTime());
        return date;
    }

    public static void main(String[] args) {
        String timeDay = getTimeDay("yyyy-MM-dd hh:mm:ss", -1);
        System.out.println(timeDay);
    }

    //public static void main(String[] args) throws JSONException {
    //    /**
    //     * data 组装
    //     */
    //
    //    //String url = "https://cgcallback-fat.ctripqa.com/nemoweb/ad/common/outAdMonitor.do";
    //
    //    String appId = "bairong001";
    //    String key = "f3df6f62f0527bf0";
    //    String iv = "3b2dac323465b024";
    //    String singKey = "95cc01ec07387a44";
    //
    //    String source = "BaiRong_C01";
    //    String actionType = "SMS";
    //    String channel = "commonOutAdMonitor";
    //
    //    JSONObject deviceInfo = new JSONObject();
    //    deviceInfo.put("sha256Tel","AEAA638C17D05884717153C3C898A41598E87209AC42E4CFCD844F6E724C33D7");
    //
    //    //String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
    //    String timestemp = "1660283657";
    //    ThirdAdOuterReq thirdAdOuterReq = new ThirdAdOuterReq(timestemp,source,"202208110117312",actionType,deviceInfo.toString());
    //
    //
    //    Map<String,Object> retMap = Maps.newHashMap();
    //    retMap.put("appId",appId);
    //    retMap.put("timestamp",timestemp);
    //    retMap.put("channel",channel);
    //    retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(thirdAdOuterReq),key,iv));
    //    retMap.put("sign",FinanceAESUtils.signLocal(retMap,singKey));
    //    System.out.println(retMap);
    //    //https://cgcallback-fat.ctripqa.com/nemoweb/ad/common/outAdMonitor.do
    //    System.out.println(JSON.toJSONString(retMap));
    //}
}
