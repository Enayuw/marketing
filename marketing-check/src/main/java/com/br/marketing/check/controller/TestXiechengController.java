package com.br.marketing.check.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.xiecheng.FinanceAESUtils;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
import com.br.marketing.entity.ThirdAdOuterReq;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.service.MarketingSmyPushService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @GetMapping("/resultVolumeCheck")
    public void process() {
        PhoneSaleExtendInfoExample updateExample = new PhoneSaleExtendInfoExample();
        List<Long> ids = new ArrayList<>();
        ids.add(690057L);
        updateExample.createCriteria().andIdIn(ids);
        PhoneSaleExtendInfo updateEntity = new PhoneSaleExtendInfo();
        updateEntity.setPStatus(2);
        phoneSaleExtendInfoMapper.updateByExampleSelective(updateEntity,updateExample);
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
