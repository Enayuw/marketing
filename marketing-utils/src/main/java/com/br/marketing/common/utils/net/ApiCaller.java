package com.br.marketing.common.utils.net;

import com.alibaba.fastjson.JSON;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;

//@Component
//@Scope(proxyMode=ScopedProxyMode.TARGET_CLASS,value = "prototype")
public class ApiCaller {

    public ApiCaller(){
        restTemplate = new RestTemplate();
    }

    public ApiCaller(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    private RestTemplate restTemplate;

    private String url;

    private Object requestParam;

    private HashMap requestHeader;

    protected MediaType contentType;

    protected HttpHeaders httpHeaders;

    protected String encodeName = "utf-8";

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

    public ApiCaller setHttpHeaders(HashMap<String,String> headers) {
        headers.keySet().forEach(t->{
            this.httpHeaders.add(t,headers.get(t));
                });
        return this;
    }

    public String get(){
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, createHttpEntity(), String.class);
        return exchange.getBody();
    }

    public ThirdApiResultTransfer postTransferStr(){
        System.out.println("POST====="+this);
        HttpEntity postHttpEntity = createPostHttpEntity();
        System.out.println(url);
        System.out.println(postHttpEntity.getBody());
        ThirdApiResultTransfer transfer = new ThirdApiResultTransfer();
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, postHttpEntity, String.class);
        transfer.setHttpCode(stringResponseEntity.getStatusCodeValue());
        transfer.setResult(stringResponseEntity.getBody());
        return transfer;
    }

    
    private HttpEntity createHttpEntity(){
        HttpEntity requestEntity = new HttpEntity<String>(null, httpHeaders);
        return requestEntity;
    }

    private HttpEntity createPostHttpEntity(){
        HttpEntity requestEntity;
        if (requestParam instanceof String) {
            requestEntity = new HttpEntity<String>((String) requestParam, httpHeaders);
        } else if (contentType.isCompatibleWith(MediaType.APPLICATION_JSON_UTF8)) {
            requestEntity = new HttpEntity<String>(JSON.toJSONString(requestParam), httpHeaders);
        } else if(contentType.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)){
            requestEntity = new HttpEntity<String>(CallUtils.getFormUrlEncodedStr(requestParam, encodeName), httpHeaders);
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
