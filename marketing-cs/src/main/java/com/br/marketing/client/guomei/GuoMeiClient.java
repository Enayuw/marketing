package com.br.marketing.client.guomei;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.HttpProxyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 国美客户端
 *
 * @author Hua Qiang
 * @date 2024-08-20 14:06
 */
@Component
@Slf4j
public class GuoMeiClient {

    @Value("${api.guomei.userDataCallBack.url:https://united-umg-t1.gomefinance.com.cn/umg/api/v1/agencyOperation/pushDataCallback}")
    private String pushDataCallbackUrl;

    @Value("${api.guomei.userDataCallBack.url:https://united-umg-t1.gomefinance.com.cn/umg/api/v1/agencyOperation/pushResultCallback}")
    private String pushResultCallbackUrl;

    @Value("${api.guomei.isProxy:false}")
    private boolean isProxy;

    @Resource
    private HttpProxyClient httpProxyClient;

    /**
     * 2024-08-20 19:38
     * 用户数据回传接口：（所有接收到的用户数据）
     * 1：批量推送：每次最多 1000 条
     * 2：用户为 0 的情况下，不需要调用接口
     * 3：如果有重推需保证 requestId 不变
     *
     * @param userDataCallBackRequest 用户数据
     * @return GmCallBackResponse
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public GmCallBackResponse<?> sendUserDataCallBack(GmUserDataCallBackRequest userDataCallBackRequest) {
        Map<String, String> map = httpProxyClient.sendByCodeZw(userDataCallBackRequest
                , pushDataCallbackUrl, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, "");
        return getResponse(map);
    }

    /**
     * 2024-08-20 19:38
     * <p>
     * 营销结果数据回传接口
     * 1：批量推送：每次 1000 条
     * 2：如果有重推需保证 requestId 不变
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public GmCallBackResponse<?> sendMarketingResultCallBack(GmMarketingResultCallBackRequest marketingResultCallBackRequest) {
        Map<String, String> map = httpProxyClient.sendByCodeZw(marketingResultCallBackRequest
                , pushResultCallbackUrl, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, "");
        return getResponse(map);
    }

    /**
     * 2024-08-20 21:05
     * 获取响应数据
     *
     * @return httpcode 非正常时返回null
     */
    private GmCallBackResponse<?> getResponse(Map<String, String> map) {
        String httpCode = map.getOrDefault("httpcode", "");
        if (httpCode.startsWith("2") || httpCode.startsWith("3")) {
            String respStr = map.getOrDefault("content", "");
            return JSON.parseObject(respStr, new TypeReference<GmCallBackResponse<?>>() {
            });
        }
        return null;
    }

}
