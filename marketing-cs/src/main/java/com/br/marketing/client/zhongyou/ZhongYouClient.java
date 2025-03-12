package com.br.marketing.client.zhongyou;

import com.alibaba.fastjson.JSON;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.ChallengeState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 描述：： 中邮接口请求
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYouClient
 * @author: it-yml
 * @create: 2023-07-27 22:34
 * @Version 1.0
 * --------------------------------------
 **/

@Service
@Slf4j
public class ZhongYouClient {

    private static final String CHARSET_UTF8 = "UTF-8";
    @Value("${otherConfig.proxy.proxy_host:00}")
    private String proxyHost;
    @Value("${otherConfig.proxy.proxy_host_zw:00}")
    private String proxyHostZW;
    @Value("${otherConfig.proxy.proxy_port:00}")
    private int proxyPort;
    @Value("${otherConfig.proxy.proxy_username:00}")
    private String userName;
    @Value("${otherConfig.proxy.proxy_password:00}")
    private String password;
    private static final PoolingHttpClientConnectionManager HTTP_CLIENT_POOL = new PoolingHttpClientConnectionManager();

    static {
        HTTP_CLIENT_POOL.setMaxTotal(5000);
        HTTP_CLIENT_POOL.setDefaultMaxPerRoute(500);
    }

    @Resource
    MarketingCommonConfig marketingCommonConfig;


    @Resource
    InterfaceLogMapper interfaceLogMapper;

    @Qualifier("interfaceLogDbpool")
    @Resource
    ThreadPoolExecutor interfaceLogDbpool;

    @Resource
    ZhongYouResultInterface zhongYouResultInterface;


    /**
     * 中邮接口请求调用
     * @param param 参数
     * @param url 地址
     * @param isProxy 是否dialing
     * @param isStream 是否是数据流
     * @param fileId 文件id
     * @return
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public HashMap<String, String> sendByCodeWithLog(Object param, String url, Boolean isProxy, Boolean isStream,Long fileId) {
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setUrl(url);
        interfaceLog.setCreateTime(new Date());
        HttpClient httpClient = getHttpClientInner(isProxy);

        HashMap<String, String> res = new HashMap<>();
        long start = System.currentTimeMillis();
        try {
            HttpPost post = new HttpPost(url);
            String paramString = JSON.toJSONString(param);
            interfaceLog.setRequestParam(paramString);
            post.setEntity( new StringEntity(paramString, CHARSET_UTF8));
            post.setHeader("content-type", MediaType.APPLICATION_JSON_UTF8_VALUE);
            post.setHeader("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
            interfaceLog.setHeader(Arrays.toString(post.getAllHeaders()));
            post.setConfig(getRequestConfig(isProxy));
            HttpResponse response;
            start = System.currentTimeMillis();
            if (isProxy) {
                AuthCache authCache = new BasicAuthCache();
                AuthScheme authScheme = new BasicScheme(ChallengeState.PROXY);
                authCache.put(new HttpHost(proxyHost, proxyPort), authScheme);
                HttpContext httpContext = new BasicHttpContext();
                httpContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
                response = httpClient.execute(post, httpContext);
            } else {
                response = httpClient.execute(post);
            }
            long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                Map<String,String > resultMap;
                if (isStream) {
                    resultMap = zhongYouResultInterface.applyStream(response.getEntity().getContent(),fileId);
                } else {
                    resultMap = zhongYouResultInterface.applyEntity(response.getEntity());
                }
                interfaceLog.setResult( resultMap.get("result"));
                res.put("content", resultMap.get("responseData"));
            }
            res.put("httpcode", String.valueOf(statusCode));
            interfaceLog.setHttpCode(statusCode);
            post.releaseConnection();
        } catch (Exception e) {
            log.error("url={} param={}", url, param, e);
            long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
            interfaceLog.setResult(e.getMessage());
            res.put("content", e.getMessage());
        }

        saveInterfaceLog(interfaceLog);

        return res;
    }

    /**
     * interface-log 存储
     * @param interfaceLog
     */
    private void saveInterfaceLog(InterfaceLog interfaceLog) {
        interfaceLogDbpool.submit(() -> {
            try {
                interfaceLogMapper.insertSelective(interfaceLog);
            } catch (Exception ex) {
                log.error(String.format("插入接口日志报错:%s", ex.getMessage()), ex);
            }
        });
    }


    private HttpClient getHttpClientInner(Boolean isProxy) {
        if (isProxy) {
            // 设置代理HttpHost
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            // 设置认证
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(userName, password));
            return HttpClients.custom().setConnectionManager(HTTP_CLIENT_POOL).setDefaultCredentialsProvider(provider).build();
        } else {
            return HttpClientBuilder.create().setConnectionManager(HTTP_CLIENT_POOL).build();
        }
    }


    /**
     * 配置信息
     *
     * @param isProxy 是否代理
     * @return RequestConfig requestConfig
     */
    private RequestConfig getRequestConfig(Boolean isProxy) {
        if (isProxy) {
            return RequestConfig.custom()
                    .setSocketTimeout(60000)
                    .setConnectTimeout(60000)
                    .setProxy(new HttpHost(proxyHost, proxyPort))
                    .setConnectionRequestTimeout(60000)
                    .build();
        } else {
            return RequestConfig.custom()
                    .setSocketTimeout(60000)
                    .setConnectTimeout(60000)
                    .setConnectionRequestTimeout(60000)
                    .build();
        }
    }
}
