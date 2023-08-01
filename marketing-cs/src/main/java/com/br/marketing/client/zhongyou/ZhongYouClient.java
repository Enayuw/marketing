package com.br.marketing.client.zhongyou;

import com.alibaba.fastjson.JSON;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.mapper.InterfaceLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
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
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

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

    @Autowired
    MarketingCommonConfig marketingCommonConfig;


    @Autowired
    InterfaceLogMapper interfaceLogMapper;

    @Qualifier("interfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor interfaceLogDbpool;

    public HashMap<String, String> sendByCodeWithLog(Object param, String url, Boolean isPorxy, String mediaType, String extendInfo, Boolean isDbLog, Boolean isStream) {
        return sendByCodePool(param, url, isPorxy, mediaType, extendInfo, isDbLog,isStream);
    }


    private HashMap<String, String> sendByCodePool(Object param, String url, Boolean isPorxy, String mediaType, String extendInfo, Boolean isDbLog,Boolean isStream) {
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setExtendInfo(extendInfo);
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setUrl(url);
        interfaceLog.setCreateTime(new Date());
        HttpClient httpClient = getHttpClientInner(isPorxy);
        HashMap<String, String> res = new HashMap<>();
        long start = System.currentTimeMillis();
        try {
            HttpPost post = new HttpPost(url);
            HttpEntity requestEntity;
            if (mediaType.equals(MediaType.APPLICATION_JSON_UTF8_VALUE)) {
                String s = JSON.toJSONString(param);
                interfaceLog.setRequestParam(s);
                requestEntity = new StringEntity(s, CHARSET_UTF8);
            } else {
                throw new RuntimeException("不支持的请求类型");
            }
            post.setEntity(requestEntity);
            post.setHeader("content-type", mediaType);
            interfaceLog.setHeader(Arrays.toString(post.getAllHeaders()));
            RequestConfig requestConfig = getRequestConfig(isPorxy, 10000, null);
            post.setConfig(requestConfig);
            HttpResponse response;
            start = System.currentTimeMillis();
            if (isPorxy) {
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
                if(isStream){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String tempString;
                    int line = 1;
                    while ((tempString = reader.readLine()) != null) {
                        String lineData = tempString.trim();
                        // 如果第一行返回是一个json 格式则说明接口请求异常
                        if (line == 1) {
                            if (isValidJson(lineData)) {
                                interfaceLog.setResult(lineData);
                                res.put("content", lineData);
                                // 异常数据停止循环
                                log.error("中邮文件拉取数据异常：{} ", lineData);
                                break;
                            }
                            if (isNumeric(lineData)) {
                                // 设置第一行数据标记
                            }
                        }
                        // 存储数据
                        System.out.println("--- 第" + line + "行 ---");
                        System.out.println(lineData);
                        line++;
                    }
                }else {
                    String result = EntityUtils.toString(response.getEntity(), CHARSET_UTF8);
                    res.put("content", result);
                    interfaceLog.setResult(result);
                }

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
        if (isDbLog) {
            interfaceLogDbpool.submit(() -> {
                try {
                    interfaceLogMapper.insertSelective(interfaceLog);
                } catch (Exception ex) {
                    log.error(String.format("插入接口日志报错:%s", ex.getMessage()), ex);
                }
            });
        }
        return res;
    }

    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private static boolean isValidJson(String json) {
        try {
            new ObjectMapper().readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
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
    public RequestConfig getRequestConfig(Boolean isProxy, Integer sockTimeout, Integer proxyType) {
        if (isProxy) {
            return RequestConfig.custom()
                    .setSocketTimeout(sockTimeout)
                    .setConnectTimeout(5000)
                    .setProxy(new HttpHost(new Integer(1).equals(proxyType) ? proxyHostZW : proxyHost, proxyPort))
                    .setConnectionRequestTimeout(5000)
                    .build();
        } else {
            return RequestConfig.custom()
                    .setSocketTimeout(sockTimeout)
                    .setConnectTimeout(1000)
                    .setConnectionRequestTimeout(1000)
                    .build();
        }
    }
}
