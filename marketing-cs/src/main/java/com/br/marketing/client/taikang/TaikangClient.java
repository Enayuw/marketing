package com.br.marketing.client.taikang;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.taikang.util.ChannelRequest;
import com.br.marketing.client.taikang.util.SimpleDataPackToolsV2;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.aes.AesTaiKang;
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

    public String process(TaikangMarketingEvent taikangMarketingEvent) {
        Map<String, String> taikangConfig = marketingCommonConfig.getTaikangConfig();
        try {
            enrichEventWithConfig(taikangMarketingEvent, taikangConfig);
            ChannelRequest channelRequest = buildChannelRequest(taikangMarketingEvent, taikangConfig);
            Header[] headers = buildHeaders(taikangConfig);
            HashMap<String, String> result = httpProxyClient.sendByCodePoolTaikang(
                    channelRequest,
                    taikangConfig.get("url"),
                    true,
                    JSON.toJSONString(taikangMarketingEvent),
                    headers);
            return JSONObject.toJSONString(result);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAIKANG_MARKING_SERVICEERROR.getCode(),
                    "调用泰康营销事件失败,eventId=" + taikangMarketingEvent.getEventId()), e);
        }
        return null;
    }

    private void enrichEventWithConfig(TaikangMarketingEvent taikangMarketingEvent, Map<String, String> config) {
        taikangMarketingEvent.setApplicantPhone(
                AesTaiKang.AesEncrypt(
                        taikangMarketingEvent.getApplicantPhone(),
                        normalizeKey(config.get("aesKey"))));
        taikangMarketingEvent.setEventId(config.get("eventId"));
        taikangMarketingEvent.setChannelCode(config.get("channelCode"));
    }

    private ChannelRequest buildChannelRequest(TaikangMarketingEvent taikangMarketingEvent, Map<String, String> config) {
        SimpleDataPackToolsV2 dataPackTools = new SimpleDataPackToolsV2();
        return dataPackTools.clientPacking(
                normalizeKey(config.get("remotePublicKey")),
                normalizeKey(config.get("localPrivateKey")),
                taikangMarketingEvent);
    }

    private Header[] buildHeaders(Map<String, String> config) {
        return new Header[]{
                new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8"),
                new BasicHeader("caller", config.get("caller"))
        };
    }

    private String normalizeKey(String key) {
        return key == null ? null : key.replace("*", "=");
    }
}
