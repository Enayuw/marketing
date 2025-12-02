package com.br.marketing.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.BrCipherMaker;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


/**
 * HttpProxyClient
 */
@Service
@Slf4j
public class HttpProxyClient {
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

    public Map<String, Object> request(String url, String data, Boolean isProxy) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            log.warn("http发送数据入参--请求地址:{},参数：{}", url, data);
            String result = send(data, url, isProxy);
            log.warn("http发送数据返回结果{}", result);
            resultMap.put("data", result);
            if (StringUtils.isNotBlank(result)) {
                JSONObject resultJson = JSONObject.parseObject(result);
                if ("00".equalsIgnoreCase(resultJson.getString("code"))) {
                    resultMap.put("result", Boolean.TRUE);
                    return resultMap;
                } else {
                    resultMap.put("result", Boolean.FALSE);
                    resultMap.put("desc", "状态码错误" + resultJson.getString("code"));
                    return resultMap;
                }
            } else {
                resultMap.put("result", Boolean.FALSE);
                resultMap.put("desc", "http请求返回结果为空");
                return resultMap;
            }
        } catch (Exception e) {
            log.error("http发送数据异常", e);
            resultMap.put("result", Boolean.FALSE);
            resultMap.put("desc", "http请求异常，可能地址不正确或服务不可用");
            return resultMap;
        }
    }

    /**
     * http发送
     *
     * @param param 参数
     * @param url   发送地址
     * @return String 返回信息
     */
    public String send(String param, String url, Boolean isPorxy) {
        HttpClient httpClient = getHttpClient(isPorxy, null);
        try {
            HttpPost post = new HttpPost(url);
            HttpEntity requestEntity = new StringEntity(param, CHARSET_UTF8);
            post.setEntity(requestEntity);
            post.setHeader("content-type", "application/json");
            RequestConfig requestConfig = getRequestConfig(isPorxy);
            post.setConfig(requestConfig);
            HttpResponse response = httpClient.execute(post);
            String result = EntityUtils.toString(response.getEntity());
            post.releaseConnection();
            return result;
        } catch (IOException e) {
            log.error("url={} param={}", url, param, e);
            return null;
        }
    }

    /**
     * http发送
     *
     * @param param 参数
     * @param url   发送地址
     * @return String 返回信息
     */
    public HashMap<String, String> sendByCode(String param, String url, Boolean isPorxy) {
        HttpClient httpClient = getHttpClient(isPorxy, null);
        HashMap<String, String> res = new HashMap<>();
        try {
            HttpPost post = new HttpPost(url);
            HttpEntity requestEntity = new StringEntity(param, CHARSET_UTF8);
            post.setEntity(requestEntity);
            post.setHeader("content-type", "application/json");
            RequestConfig requestConfig = getRequestConfig(isPorxy, 10000, null);
            post.setConfig(requestConfig);
            HttpResponse response = null;
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

            int statusCode = response.getStatusLine().getStatusCode();
            res.put("httpcode", String.valueOf(statusCode));
            String result = EntityUtils.toString(response.getEntity());
            res.put("content", result);
            post.releaseConnection();
        } catch (Exception e) {
            log.error("url={} param={}", url, param, e);
            res.put("content", e.getMessage());
        }
        return res;
    }

    public HttpResponse downloadFile(String fileUrl, Boolean isPorxy) {
        HttpResponse response = null;
        try {
            HttpClient httpClient = getHttpClient(isPorxy, null);
            HttpGet httpGet = new HttpGet(fileUrl);

            RequestConfig requestConfig = getRequestRedirectsConfig(isPorxy, 50000, null);
            httpGet.setConfig(requestConfig);

            // 设置请求头 模拟浏览器请求
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            // 发送请求
            response = httpClient.execute(httpGet);

            // 处理重定向
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 301 || statusCode == 302) {
                String redirectUrl = response.getFirstHeader("Location").getValue();
                redirectUrl = redirectUrl.replaceAll("[^\\x00-\\x7F]+", "需求");
                // 重新发送请求到重定向 URL
                HttpGet get = new HttpGet(redirectUrl);
                get.setConfig(getRequestConfig(isPorxy, 50000, null));
                // 设置请求头 模拟浏览器请求
                get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

                response = httpClient.execute(get);
            }
        }catch (Exception e){
            log.error("车线索每日文档下载失败："+e.getMessage());
        }
        return response;
    }

    @Autowired
    InterfaceLogMapper interfaceLogMapper;

    @Qualifier("interfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor interfaceLogDbpool;

    public HashMap<String, String> sendByCodeWithLog(Object param, String url, Boolean isPorxy, String mediaType, String extendInfo, Boolean isDbLog, Boolean isFileLog) {
        return sendByCodePool(param, url, isPorxy, mediaType, extendInfo, isDbLog, isFileLog);
    }

    public HashMap<String, String> sendByCodeZw(Object param, String url, Boolean isPorxy, String mediaType, String extendInfo) {
        return sendByCode(param, url, isPorxy, mediaType, extendInfo, true, false, 1);
    }

    public HashMap<String, String> sendByCode(Object param, String url, Boolean isPorxy, String mediaType, String extendInfo) {
        return sendByCode(param, url, isPorxy, mediaType, extendInfo, true, false, null);
    }

    public HashMap<String, String> sendByCodeWithLogWithHeader(Object param, String url, Boolean isPorxy, String mediaType, String extendInfo, Boolean isDbLog, Boolean isFileLog,Header[] headers) {
        return sendByCodePoolWithHeader(param, url, isPorxy, mediaType, extendInfo, isDbLog, isFileLog,headers);
    }

    /**
     * 调用接口的核心方法
     *
     * @param param      参数
     * @param url        请求地址
     * @param isPorxy    是否通过代理访问
     * @param mediaType  数据格式
     * @param extendInfo 扩展信息
     * @param isDbLog    日志是否存储在db中
     * @param isFileLog  日志是否存储在log中
     * @param proxyType  代理的机房 1-兆维代理；2-亦庄代理；
     * @return
     */
    private HashMap<String, String> sendByCode(Object param, String url, Boolean isPorxy, String mediaType, String extendInfo, Boolean isDbLog, Boolean isFileLog, Integer proxyType) {
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setExtendInfo(extendInfo);
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setUrl(url);
        interfaceLog.setCreateTime(new Date());
        HttpClient httpClient = getHttpClient(isPorxy, proxyType);
        HashMap<String, String> res = new HashMap<>();
        Long start = System.currentTimeMillis();
        try {
            HttpPost post = new HttpPost(url);
            HttpEntity requestEntity = null;
            if (mediaType.equals(MediaType.APPLICATION_JSON_UTF8_VALUE)) {
                String s = JSON.toJSONString(param);
                interfaceLog.setRequestParam(s);
                requestEntity = new StringEntity(s, CHARSET_UTF8);
            } else if (mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                StringBuilder paramStr = new StringBuilder();
                BeanMap beanMap = BeanMap.create(param);
                for (Object o : beanMap.keySet()) {
                    Object value = beanMap.get(o);
                    if (value != null) {
                        paramStr.append(String.format("%s=%s&", o.toString(), URLEncoder.encode(beanMap.get(o).toString(), "utf-8")));
                    }
                }
                interfaceLog.setRequestParam(paramStr.toString());
                requestEntity = new StringEntity(paramStr.toString(), CHARSET_UTF8);
            } else {
                throw new RuntimeException("不支持的请求类型");
            }
            post.setEntity(requestEntity);
            post.setHeader("content-type", mediaType);
            interfaceLog.setHeader(post.getAllHeaders().toString());
            RequestConfig requestConfig = getRequestConfig(isPorxy, 10000, proxyType);
            post.setConfig(requestConfig);
            HttpResponse response = null;
            start = System.currentTimeMillis();
            if (isPorxy) {
                AuthCache authCache = new BasicAuthCache();
                AuthScheme authScheme = new BasicScheme(ChallengeState.PROXY);
                authCache.put(new HttpHost(Integer.valueOf(1).equals(proxyType) ? proxyHostZW : proxyHost, proxyPort), authScheme);
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
            String result = EntityUtils.toString(response.getEntity(), CHARSET_UTF8);
            res.put("content", result);
            interfaceLog.setResult(result);
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
            } else if (mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                StringBuilder paramStr = new StringBuilder();
                BeanMap beanMap = BeanMap.create(param);
                for (Object o : beanMap.keySet()) {
                    paramStr.append(String.format("%s=%s&", o.toString(), URLEncoder.encode(beanMap.get(o).toString(), "utf-8")));
                }
                interfaceLog.setRequestParam(paramStr.toString());
                requestEntity = new StringEntity(paramStr.toString(), CHARSET_UTF8);
            } else {
                throw new RuntimeException("不支持的请求类型");
            }
            post.setEntity(requestEntity);
            post.setHeader("content-type", mediaType);
            interfaceLog.setHeader(post.getAllHeaders().toString());
            RequestConfig requestConfig = getRequestConfig(isPorxy, 20000, null);
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
            String result = EntityUtils.toString(response.getEntity(), CHARSET_UTF8);
            res.put("content", result);
            interfaceLog.setResult(result);
            interfaceLog.setHttpCode(statusCode);
            post.releaseConnection();
        } catch (Exception e) {
            if (url.contains("ibu-daas")) {
                log.error("url={} Log加密param={}", url, BrCipherMaker.getInstance().encode(param.toString()), e);
            } else if (url.contains("https://finance-gateway-pop.diandian.com.cn/fcpGateway")) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(),
                        "url= " + url + " param= " + param), e);
            } else {
                log.error("url={} param={}", url, param, e);
            }
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
            if (url.contains("ibu-daas")) {
                log.warn("InterfaceLog表加密输出{}",BrCipherMaker.getInstance().encode(JSON.toJSONString(interfaceLog)));
            } else {
                log.warn(JSON.toJSONString(interfaceLog));
            }
        }
        return res;
    }
    public HashMap<String, String> sendByCodePoolTaikang(Object param, String url, Boolean isPorxy, String extendInfo,Header[] headers) {
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
            String s = JSON.toJSONString(param);
            interfaceLog.setRequestParam(s);
            HttpEntity requestEntity = new StringEntity(s,"application/json;charset=UTF-8", CHARSET_UTF8);
            post.setEntity(requestEntity);

            // ========== 修正Header设置 ==========
            // 正确设置Content-Type（推荐方式）
            post.setHeaders(headers);

            // 记录header日志（修正后的）
            String headerString = Arrays.stream(post.getAllHeaders())
                    .map(header -> header.getName() + "=" + header.getValue())
                    .collect(Collectors.joining(", "));
            interfaceLog.setHeader(headerString);
            // ========== Header设置结束 ==========

            RequestConfig requestConfig = getRequestConfig(isPorxy, 20000, null);
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
            String result = EntityUtils.toString(response.getEntity(), CHARSET_UTF8);
            res.put("content", result);
            interfaceLog.setResult(result);
            interfaceLog.setHttpCode(statusCode);
            post.releaseConnection();

        } catch (Exception e) {
            log.error("url={} param={}", url, param, e);
            Long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
            interfaceLog.setResult(e.getMessage());
            res.put("content", e.getMessage());
        }

        interfaceLogDbpool.submit(() -> {
            try {
                interfaceLogMapper.insertSelective(interfaceLog);
            } catch (Exception ex) {
                log.error(String.format("插入接口日志报错:%s", ex.getMessage()), ex);
            }
        });

        return res;
    }

    private HashMap<String, String> sendByCodePoolWithHeader(Object param, String url, Boolean isPorxy, String mediaType, String extendInfo, Boolean isDbLog, Boolean isFileLog, Header[] headers) {
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
                String s = param.toString();
                interfaceLog.setRequestParam(s);
                requestEntity = new StringEntity(s, CHARSET_UTF8);
            } else if (mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                StringBuilder paramStr = new StringBuilder();
                BeanMap beanMap = BeanMap.create(param);
                for (Object o : beanMap.keySet()) {
                    paramStr.append(String.format("%s=%s&", o.toString(), URLEncoder.encode(beanMap.get(o).toString(), "utf-8")));
                }
                interfaceLog.setRequestParam(paramStr.toString());
                requestEntity = new StringEntity(paramStr.toString(), CHARSET_UTF8);
            } else {
                throw new RuntimeException("不支持的请求类型");
            }
            post.setEntity(requestEntity);
            post.setHeaders(headers);
            interfaceLog.setHeader(post.getAllHeaders().toString());
            RequestConfig requestConfig = getRequestConfig(isPorxy, 20000, null);
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
            String result = EntityUtils.toString(response.getEntity(), CHARSET_UTF8);
            res.put("content", result);
            interfaceLog.setResult(result);
            interfaceLog.setHttpCode(statusCode);
            post.releaseConnection();
        } catch (Exception e) {
            if (url.contains("ibu-daas")) {
                log.error("url={} Log加密param={}", url, BrCipherMaker.getInstance().encode(param.toString()), e);
            } else if (url.contains("https://finance-gateway-pop.diandian.com.cn/fcpGateway")) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(),
                        "url= " + url + " param= " + param), e);
            } else {
                log.error("url={} param={}", url, param, e);
            }
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
            if (url.contains("ibu-daas")) {
                log.warn("InterfaceLog表加密输出{}",BrCipherMaker.getInstance().encode(JSON.toJSONString(interfaceLog)));
            } else {
                log.warn(JSON.toJSONString(interfaceLog));
            }
        }
        return res;
    }

    /**
     * 获取httpClient
     *
     * @param isProxy 是否代理
     * @return HttpClient httpClient
     */
    public HttpClient getHttpClient(Boolean isProxy, Integer proxyType) {
        if (isProxy) {
            if (new Integer(1).equals(proxyType)) {
                return getHttpClientZw();
            } else {
                return getHttpClientSimple();
            }
        } else {
            CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(HTTP_CLIENT_POOL).build();
            return httpClient;
        }
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

    public RequestConfig getRequestRedirectsConfig(Boolean isProxy, Integer sockTimeout, Integer proxyType) {
        if (isProxy) {
            return RequestConfig.custom()
                    .setSocketTimeout(sockTimeout)
                    .setConnectTimeout(5000)
                    .setProxy(new HttpHost(new Integer(1).equals(proxyType) ? proxyHostZW : proxyHost, proxyPort))
                    .setConnectionRequestTimeout(5000)
                    .setRedirectsEnabled(false)
                    .build();
        } else {
            return RequestConfig.custom()
                    .setSocketTimeout(sockTimeout)
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .setRedirectsEnabled(false)
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

    /**
     * @description:get请求封装
     * @author: zhen.Li1
     * @time: 2024-07-11
     */
    public HashMap<String, String> get(String uri, Boolean isPorxy, String charset) {
        HashMap<String, String> res = new HashMap<>();
        HttpClient httpClient = getHttpClientInner(isPorxy);
        if (StringUtils.isEmpty(charset)) {
            charset = CHARSET_UTF8;
        }
        try {
            HttpGet httpGet = new HttpGet(uri);
            RequestConfig requestConfig = getRequestConfig(isPorxy, 10000, null);
            httpGet.setConfig(requestConfig);
            log.warn("请求url={},proxy={}", httpGet.getURI().toString(),isPorxy);
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            res.put("httpcode", String.valueOf(statusCode));
            //toString方法有坑，不能处理编码转换
            //String result = EntityUtils.toString(response.getEntity(), CHARSET_UTF8);
            byte[] responseBodyBytes = EntityUtils.toByteArray(response.getEntity());
            String result = new String(responseBodyBytes, Charset.forName(charset));
            res.put("content", result);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.INTERFACE_ERROR.getCode(), "url=" + uri), e);
            res.put("content", e.getMessage());
        }
        return res;
    }

    /**
     * @description:get请求封装
     * @author: dongshuo.he
     * @time: 2025-07-07
     */
    public HashMap<String, String> getWithLog(String uri, Boolean isPorxy, String charset) {
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setUrl(uri);
        interfaceLog.setCreateTime(new Date());
        HashMap<String, String> res = new HashMap<>();
        HttpClient httpClient = getHttpClientInner(isPorxy);
        if (StringUtils.isEmpty(charset)) {
            charset = CHARSET_UTF8;
        }
        Long start = System.currentTimeMillis();
        try {
            HttpGet httpGet = new HttpGet(uri);
            RequestConfig requestConfig = getRequestConfig(isPorxy, 10000, null);
            httpGet.setConfig(requestConfig);
            log.warn("get请求url={},proxy={}", httpGet.getURI().toString(),isPorxy);
            HttpResponse response = httpClient.execute(httpGet);
            Long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
            int statusCode = response.getStatusLine().getStatusCode();
            res.put("httpcode", String.valueOf(statusCode));
            //toString方法有坑，不能处理编码转换
            //String result = EntityUtils.toString(response.getEntity(), CHARSET_UTF8);
            byte[] responseBodyBytes = EntityUtils.toByteArray(response.getEntity());
            String result = new String(responseBodyBytes, Charset.forName(charset));
            res.put("content", result);
            interfaceLog.setResult(result);
            interfaceLog.setHttpCode(statusCode);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.INTERFACE_ERROR.getCode(), "url=" + uri), e);
            res.put("content", e.getMessage());
            Long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
            interfaceLog.setResult(e.getMessage());
        }
        interfaceLogDbpool.submit(() -> {
            try {
                interfaceLogMapper.insertSelective(interfaceLog);
            } catch (Exception ex) {
                log.error(String.format("插入接口日志报错:%s", ex.getMessage()), ex);
            }
        });
        return res;
    }




}
