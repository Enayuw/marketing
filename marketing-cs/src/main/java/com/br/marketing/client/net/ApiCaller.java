package com.br.marketing.client.net;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.net.CallUtils;
import com.br.marketing.common.utils.net.InterfaceLog;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.rpcclient.rpcclientImpl.BrokerGrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class ApiCaller {

    public ApiCaller() {
        restTemplate = new RestTemplate();
        this.httpHeaders = new HttpHeaders();
    }

    public ApiCaller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.httpHeaders = new HttpHeaders();
    }

    public ApiCaller(RestTemplate restTemplate, BrokerGrpcClient momGrpcClient, ThreadPoolExecutor threadPoolExecutor) {
        this.restTemplate = restTemplate;
        this.httpHeaders = new HttpHeaders();
        this.momGrpcClient = momGrpcClient;
        this.logDbPool = threadPoolExecutor;
    }

    private InterfaceLog interfaceLog = new InterfaceLog();

    public ApiCaller setInterfaceLog(InterfaceLog interfaceLog) {
        this.interfaceLog = interfaceLog;
        return this;
    }

    private RestTemplate restTemplate;

    private BrokerGrpcClient momGrpcClient;

    private String url;

    private Object requestParam;

    private HashMap requestHeader;

    protected MediaType contentType;

    protected HttpHeaders httpHeaders;

    protected String encodeName = "utf-8";

    private Boolean isEncode = Boolean.FALSE;

    private ThreadPoolExecutor logDbPool;

    public ApiCaller setEncode(Boolean encode) {
        isEncode = encode;
        return this;
    }

    public void setLogPool(ThreadPoolExecutor logPool) {
        this.logDbPool = logPool;
    }

    public String getUrl() {
        return url;
    }

    public ApiCaller setUrl(String url) {
        this.url = url;
        return this;
    }

    public ApiCaller setRequestParam(Object requestParam) {
        this.requestParam = requestParam;
        return this;
    }

    public ApiCaller setRequestHeader(HashMap requestHeader) {
        this.requestHeader = requestHeader;
        return this;
    }

    public ApiCaller setContentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }


    public ApiCaller setHttpHeaders(HashMap<String, String> headers) {
        headers.keySet().forEach((String t) -> {
            this.httpHeaders.add(t, headers.get(t));
        });
        return this;
    }

    public String get() {
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, createHttpEntity(), String.class);
        return exchange.getBody();
    }

    /**
     * 弃用
     * @return com.br.marketing.common.utils.net.ThirdApiResultTransfer ThirdApiResultTransfer对象
     * @deprecated 后面尽量不要使用
     */
    @Deprecated
    public ThirdApiResultTransfer postTransferStr() {
        if (momGrpcClient != null) {
            if (StringUtils.isBlank(interfaceLog.getApiCode())) {
                throw new RuntimeException("记录接口日志 apiCode不能为空");
            }
            if (StringUtils.isBlank(interfaceLog.getSwiftNumber())) {
                throw new RuntimeException("记录接口日志 SwiftNumber不能为空");
            }
        }
        HttpEntity postHttpEntity = createPostHttpEntity();
        interfaceLog.setRequestStr(postHttpEntity.getBody().toString());
        interfaceLog.setUrl(url);
        interfaceLog.setRequestTime(new Date());
        long start = System.currentTimeMillis();
        ThirdApiResultTransfer transfer = new ThirdApiResultTransfer();
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, postHttpEntity, String.class);
        Long l = System.currentTimeMillis() - start;
        if (momGrpcClient != null) {
            try {
                interfaceLog.setCostTime(l);
                interfaceLog.setResponseStr(stringResponseEntity.getBody());
                interfaceLog.setCode(String.valueOf(stringResponseEntity.getStatusCodeValue()));
                logDbPool.submit(() -> {
                    momGrpcClient.sendInterfaceLog(interfaceLog);
                });
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        if (log.isInfoEnabled()) {
            log.info(String.format("POST=====url:%s,cost:%d,requestbody:%s,code:%d,response:%s"
                    , url, l, postHttpEntity.getBody()
                    , stringResponseEntity.getStatusCodeValue(), stringResponseEntity.getBody()));
        }
        transfer.setHttpCode(stringResponseEntity.getStatusCodeValue());
        transfer.setResult(stringResponseEntity.getBody());
        return transfer;
    }


    private HttpEntity createHttpEntity() {
        HttpEntity requestEntity = new HttpEntity<String>(null, httpHeaders);
        return requestEntity;
    }

    private HttpEntity createPostHttpEntity() {
        HttpEntity requestEntity;
        httpHeaders.setContentType(contentType);
        if (requestParam instanceof String) {
            requestEntity = new HttpEntity<String>((String) requestParam, httpHeaders);
        } else if (contentType.isCompatibleWith(MediaType.APPLICATION_JSON_UTF8)) {
            requestEntity = new HttpEntity<String>(JSON.toJSONString(requestParam), httpHeaders);
        } else if (contentType.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)) {
//            requestEntity = new HttpEntity<MultiValueMap<String, Object>>(CallUtils.getFormDataMap(requestParam), httpHeaders);
            requestEntity = new HttpEntity<String>(CallUtils.getFormUrlEncodedStr(requestParam, encodeName, isEncode), httpHeaders);
        } else if (contentType.isCompatibleWith(MediaType.MULTIPART_FORM_DATA)) {
            requestEntity = new HttpEntity<MultiValueMap<String, Object>>(CallUtils.getFormDataMap(requestParam), httpHeaders);
        } else {
            requestEntity = new HttpEntity<String>(JSON.toJSONString(requestParam), httpHeaders);
        }
        return requestEntity;
    }


    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public Object getRequestParam() {
        return requestParam;
    }

    public HashMap getRequestHeader() {
        return requestHeader;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public String getEncodeName() {
        return encodeName;
    }

    public ApiCaller setEncodeName(String encodeName) {
        this.encodeName = encodeName;
        return this;
    }
}
