package com.br.marketing.client.zhongyou;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.mapper.InterfaceLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
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
import org.springframework.cglib.beans.BeanMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;
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

    @Autowired
    MarketingCommonConfig marketingCommonConfig;


    @Autowired
    InterfaceLogMapper interfaceLogMapper;

    @Qualifier("interfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor interfaceLogDbpool;

    public HashMap<String, String> sendByCodeWithLog(Object param, String url, Boolean isPorxy, String mediaType, String extendInfo, Boolean isDbLog, Boolean isFileLog) {
        return sendByCodePool(param, url, isPorxy, mediaType, extendInfo, isDbLog, isFileLog);
    }


    private HashMap<String, String> sendByCodePool(Object param, String url, Boolean isPorxy, String mediaType, String extendInfo, Boolean isDbLog, Boolean isFileLog) {
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setExtendInfo(extendInfo);
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setUrl(url);
        interfaceLog.setCreateTime(new Date());
        HttpClient httpClient = getHttpClientInner(isPorxy);
        HashMap<String, String> res = new HashMap<>();
        Long start = System.currentTimeMillis();
        try {
            HttpPost post = new HttpPost(url);
            HttpEntity requestEntity = null;
            if (mediaType.equals(MediaType.APPLICATION_JSON_UTF8_VALUE)) {
                String s = JSON.toJSONString(param);
                interfaceLog.setRequestParam(s);
                requestEntity = new StringEntity(s, CHARSET_UTF8);
            }  else {
                throw new RuntimeException("不支持的请求类型");
            }
            post.setEntity(requestEntity);
            post.setHeader("content-type", mediaType);
            interfaceLog.setHeader(post.getAllHeaders().toString());
            RequestConfig requestConfig = getRequestConfig(isPorxy, 10000, null);
            post.setConfig(requestConfig);
            HttpResponse response = null;
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
            Long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
            int statusCode = response.getStatusLine().getStatusCode();
            res.put("httpcode", String.valueOf(statusCode));
            InputStream contentInputStream = response.getEntity().getContent();
            BufferedInputStream br = new BufferedInputStream(contentInputStream);
            String result ="";
            byte[] b = new byte[1024];
            for (int c = 0; (c = br.read(b)) != -1;) {
                result = new String(b, 0, c);
                if(isJSON(result)){
                    // 文件异常
                    interfaceLog.setResult(result);
                }
                // 保存
                System.out.println(result);
            }
            br.close();
            interfaceLog.setHttpCode(statusCode);
            post.releaseConnection();
        } catch (Exception e) {
            log.error("url={} param={}", url, param, e);
            Long end = System.currentTimeMillis();
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
        if (isFileLog) {
            log.warn(JSON.toJSONString(interfaceLog));
        }
        return res;
    }

    /**
     * 判断string 是否为 json
     * @param str
     * @return
     */
    public static boolean isJSON(String str) {
        boolean result = false;
        try {
            Object obj=JSON.parse(str);
            result = true;
        } catch (Exception e) {
            result=false;
        }
        return result;
    }

    public HttpClient getHttpClientInner(Boolean isProxy) {
        if (isProxy) {
            // 设置代理HttpHost
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            // 设置认证
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(userName, password));
            CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(HTTP_CLIENT_POOL).setDefaultCredentialsProvider(provider).build();
            return httpClient;
        } else {
            CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(HTTP_CLIENT_POOL).build();
            return httpClient;
        }
    }

    /**
     * @description:获取兆维HttpClient代理对象
     * @author: lei.zhang2@100credit.com
     * @time: 2018年6月1日 下午2:19:08
     */
    public HttpClient getHttpClientZw() {
        // 设置代理HttpHost
        HttpHost proxy = new HttpHost(proxyHostZW, proxyPort);
        // 设置认证
        CredentialsProvider provider = new BasicCredentialsProvider();

        provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(userName, password));

        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();

        return httpClient;
    }

    public HttpClient getHttpClientSimple() {
        // 设置代理HttpHost
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        // 设置认证
        CredentialsProvider provider = new BasicCredentialsProvider();

        provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(userName, password));

        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();

        return httpClient;
    }

    /**
     * 配置信息
     *
     * @param isProxy 是否代理
     * @return RequestConfig requestConfig
     */
    public RequestConfig getRequestConfig(Boolean isProxy) {
        if (isProxy) {
            return RequestConfig.custom()
                    .setSocketTimeout(6000)
                    .setConnectTimeout(1000)
                    .setProxy(new HttpHost(proxyHost, proxyPort))
                    .setConnectionRequestTimeout(1000)
                    .build();
        } else {
            return RequestConfig.custom()
                    .setSocketTimeout(6000)
                    .setConnectTimeout(1000)
                    .setConnectionRequestTimeout(1000)
                    .build();
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


    /**
     * 日志存储配置
     *
     * @param callMethod 调用方法名
     * @return List : list(0)为是否db存储，list(1)为是否elk存储
     * 默认elk存储
     */
    public List<Boolean> isLogStore(String callMethod) {
        HashMap<String, List<Boolean>> apiLogMark = marketingCommonConfig.getApiLogMark();
        ArrayList<Boolean> mark = new ArrayList<>();
        if (apiLogMark == null || !apiLogMark.containsKey(callMethod)) {
            mark.add(false);
            mark.add(true);
        } else {
            mark.add(apiLogMark.get(callMethod).get(0));
            mark.add(apiLogMark.get(callMethod).get(1));
        }
        return mark;
    }
}
