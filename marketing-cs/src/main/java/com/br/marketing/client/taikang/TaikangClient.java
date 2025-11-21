package com.br.marketing.client.taikang;

import com.alibaba.fastjson2.JSON;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.taikang.util.ChannelRequest;
import com.br.marketing.client.taikang.util.SimpleDataPackToolsV2;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.aes.AesTaiKang;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * -----------------------------
 * PackageName： com.br.marketing.client.taikang.util
 * ClassName：TaikangClient
 * Description：
 *
 * @author：it-yml CreateTime：2025-11-21
 * -----------------------------
 */
@Component
@Slf4j
public class TaikangClient {
    @Resource
    HttpProxyClient httpProxyClient;
    @Resource
    MarketingCommonConfig marketingCommonConfig;

    public void process(TaikangMarketingEvent taikangMarketingEvent) {
        try {
        Map<String, String> taiKangConfig = marketingCommonConfig.getTaiKangConfig();
        taikangMarketingEvent.setApplicantPhone(AesTaiKang.AesEncrypt(taikangMarketingEvent.getApplicantPhone(), taiKangConfig.get("aesKey").replace("*","=")));
        taikangMarketingEvent.setEventId(taiKangConfig.get("eventId"));
        taikangMarketingEvent.setChannelCode(taiKangConfig.get("channelCode"));
            SimpleDataPackToolsV2 simpleDataPackToolsV2 = new SimpleDataPackToolsV2();
            ChannelRequest channelRequest = simpleDataPackToolsV2.clientPacking(
                    taiKangConfig.get("remotePublicKey").replace("*","=") ,
                    taiKangConfig.get("localPrivateKey").replace("*","=") ,
                    taikangMarketingEvent);
            Header[] headers = new Header[] {
                    new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8"),
                    new BasicHeader("caller", taiKangConfig.get("caller")),
            };
            HashMap<String, String> result = httpProxyClient.sendByCodePoolTaikang(channelRequest,  taiKangConfig.get("url"), true, JSON.toJSONString(taiKangConfig),headers);
            log.warn("荣达泰康返回结果:{}",result);
        } catch (Exception e) {
            log.warn("异常：{}",e.getMessage());
        }
    }
}
