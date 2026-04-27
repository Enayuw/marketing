package com.br.marketing.client.wuba;

import com.br.marketing.client.HttpProxyClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 58AI转化数据接口客户端 (重构版)
 * 使用项目内的 HttpProxyClient 以支持代理等功能
 */
@Slf4j
@Service
public class WuBaAIClient {

    @Value("${api.wuba.transferUrl:https://gateway-convenientloan.58v5.cn/thirdpartnar/v1/getConversionList}")
    private String transferUrl;

    @Value("${api.wuba.isProxy:false}")
    private Boolean isProxy;

    @Resource
    private HttpProxyClient httpProxyClient;


    public HttpResponse downloadConversionFile(String orgCode, String password) {
        String requestUrl = transferUrl;
        String encodedUrl;
        try {
            encodedUrl = String.format("%s?orgCode=%s&password=%s",
                    requestUrl,
                    java.net.URLEncoder.encode(orgCode, "UTF-8"),
                    java.net.URLEncoder.encode(password, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            log.error("[58AI Client] URL参数编码失败", e);
            encodedUrl = String.format("%s?orgCode=%s&password=%s", requestUrl, orgCode, password);
        }

        HttpResponse response = httpProxyClient.downloadFile(encodedUrl, isProxy);
        if (response == null) {
            log.error("[58AI Client] 调用HttpProxyClient.downloadFile返回的HttpResponse为null");
            return null;
        }
        int statusCode = response.getStatusLine().getStatusCode();
        log.info("[58AI Client] 接口响应状态码: {}", statusCode);
        return response;
    }
}
