package com.br.marketing.xcloop.job.taikang;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.util.aes.AesTaiKang;
import com.br.marketing.xcloop.job.taikang.util.ChannelRequest;
import com.br.marketing.xcloop.job.taikang.util.SimpleDataPackToolsV2;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据推送泰康
 *
 */
@Component
@Slf4j
public class TaiKangPushDataCustomerJob extends AbstractSimpleElasticJob {

    @Resource
    HttpProxyClient httpProxyClient;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("泰康job启动");

        try {
            Map<String,Object> map = new HashMap<>();
            String s = AesTaiKang.AesEncrypt("13654008330", "nX7zCFT1HaUllNbM");
            map.put("applicantPhone", s);
            map.put("eventId", "channel_browse_interruption");
            map.put("channelCode", "RongDa");
            map.put("applicantName", "");
            map.put("browseDate", "2025-11-20 12:04:00");
            SimpleDataPackToolsV2 simpleDataPackToolsV2 = new SimpleDataPackToolsV2();
            ChannelRequest channelRequest = simpleDataPackToolsV2.clientPacking(
                    "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEs9zja+l2Fd9B664O1q1Oy4fsiEoLhNiBS9zhKPuUI075vZ/dADBdE2zMbCP5oVDFBOter9IH/C1iX8C2HFrl0w==",
                    "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgaOxhL7Oj8kLi8zpgXaGJIyfBOjxzqVf68ITblLXsYIOgCgYIKoEcz1UBgi2hRANCAATjyRdmnS4msSglH4Vv9QdLyC7Bl1Em8myRlzVqKmU9+pSYIPAqv8F4sIn9eYz9XHObW1aIcH4uqHeK6TYtSoQj",
                    map);
            HashMap<String, String> send = httpProxyClient.sendByCodePoolTaikang(channelRequest, "http://49.233.178.183/e/channel/dataReplay", true, JSON.toJSONString(map));
        } catch (Exception e) {
            log.warn("调用异常",e);
        }
    }

}
