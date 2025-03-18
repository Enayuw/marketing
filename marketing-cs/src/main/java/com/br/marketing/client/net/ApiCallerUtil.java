package com.br.marketing.client.net;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.utils.net.CallUtils;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class ApiCallerUtil {

    public ApiCallerUtil() {
        restTemplate = new RestTemplate();
        this.httpHeaders = new HttpHeaders();
    }

    public ApiCallerUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.httpHeaders = new HttpHeaders();
    }

    public ApiCallerUtil(RestTemplate restTemplate, InterfaceLogMapper interfaceLogMapper,ThreadPoolExecutor threadPoolExecutor) {
        this.restTemplate = restTemplate;
        this.httpHeaders = new HttpHeaders();
        this.logDbPool = threadPoolExecutor;
        this.interfaceLogMapper = interfaceLogMapper;
    }

    private RestTemplate restTemplate;

    private String url;

    private Object requestParam;

    private HashMap requestHeader;

    protected MediaType contentType;

    protected HttpHeaders httpHeaders;

    protected String encodeName = "utf-8";

    private InterfaceLogMapper interfaceLogMapper;

    private ThreadPoolExecutor logDbPool;

    private String desc;

    private Boolean isEncode = Boolean.FALSE;

    public ApiCallerUtil setEncode(Boolean encode) {
        isEncode = encode;
        return this;
    }

    public ApiCallerUtil setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ApiCallerUtil setUrl(String url) {
        this.url = url;
        return this;
    }

    public ApiCallerUtil setRequestParam(Object requestParam) {
        this.requestParam = requestParam;
        return this;
    }

    public ApiCallerUtil setRequestHeader(HashMap requestHeader) {
        this.requestHeader = requestHeader;
        return this;
    }

    public ApiCallerUtil setContentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }

    public ApiCallerUtil setHttpHeaders(HashMap<String, String> headers) {
        headers.keySet().forEach((String t) -> {
            this.httpHeaders.add(t, headers.get(t));
        });
        return this;
    }

    public String get() {
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, createHttpEntity(), String.class);
        return exchange.getBody();
    }

    public ResponseEntity<String> getReponse(String paramJson) {
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setRequestParam(paramJson);
        interfaceLog.setUrl(url);
        interfaceLog.setExtendInfo(paramJson);
        interfaceLog.setCreateTime(new Date());
        long start = System.currentTimeMillis();
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, createHttpEntity(), String.class);
        Long l = System.currentTimeMillis() - start;
        if(interfaceLogMapper != null) {
            try {
                interfaceLog.setResult(exchange.getBody());
                interfaceLog.setHttpCode(exchange.getStatusCodeValue());
                interfaceLog.setExpire(l.toString());
                logDbPool.submit(()->{
                    interfaceLogMapper.insertSelective(interfaceLog);
                });
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        return exchange;
    }

    public ThirdApiResultTransfer postTransferStr() {
        HttpEntity postHttpEntity = createPostHttpEntity();
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setRequestParam(postHttpEntity.getBody().toString());
        interfaceLog.setUrl(url);
        interfaceLog.setHeader(postHttpEntity.getHeaders().toString());
        interfaceLog.setExtendInfo(desc);
        interfaceLog.setCreateTime(new Date());
        long start = System.currentTimeMillis();
        ThirdApiResultTransfer transfer = new ThirdApiResultTransfer();
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, postHttpEntity, String.class);
        Long l = System.currentTimeMillis() - start;
        if(interfaceLogMapper != null) {
            try {
                interfaceLog.setResult(stringResponseEntity.getBody());
                interfaceLog.setHttpCode(stringResponseEntity.getStatusCodeValue());
                interfaceLog.setExpire(l.toString());
                logDbPool.submit(()->{
                    interfaceLogMapper.insertSelective(interfaceLog);
                });
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        if(log.isInfoEnabled()) {
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
            requestEntity = new HttpEntity<String>(CallUtils.getFormUrlEncodedStr(requestParam, encodeName,isEncode), httpHeaders);
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

    public ApiCallerUtil setEncodeName(String encodeName) {
        this.encodeName = encodeName;
        return this;
    }
}
