package com.br.marketing.service.Impl.umeng;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.UMengCryptoUtil;
import com.br.marketing.entity.UMengInterfaceLog;
import com.br.marketing.mapper.UMengInterfaceLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class UMengApiServiceImpl implements IUMengApiService {
    private final static String TITLE = "【uMeng-API调用】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private UMengInterfaceLogMapper umengInterfaceLogMapper;

    @Resource
    private HttpProxyClient httpProxyClient;

    private final int socketTimeout = 30000;

    @Override
    public Result createTimingTask(Long localId,String apiCode, String requestParam,Boolean isProxy) {
        Result result = new Result().failure();
        JSONObject resultData = null;

        String rid = UUID.randomUUID().toString();
        String bizId = marketingCommonConfig.getUMengBizInfoMap().get("bizId");
        String bizSecret = marketingCommonConfig.getUMengBizInfoMap().get("bizSecret");
        String encodeBody = UMengCryptoUtil.encryptBody(bizSecret, requestParam);
        String sign = UMengCryptoUtil.getRequestSign(bizId,bizSecret,encodeBody, rid);
        String realRequestUrl = marketingCommonConfig.getUMengUrlInfoMap().get("timingTaskUrl")+"="+sign;

        HttpClient httpClient = httpProxyClient.getHttpClientInner(isProxy);
        RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, socketTimeout, null);
        HttpPost httpPost = new HttpPost(realRequestUrl);
        httpPost.setConfig(requestConfig);
        log.warn("TITLE:{} 开始,localId:{}, apiCode:{},url:{} ",TITLE,localId,apiCode,realRequestUrl);
        try {
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("bizid", bizId);
            httpPost.setHeader("rid", rid);
            String header= "Content-Type: application/json; bizid:"+bizId+"; rid:"+rid;
            StringEntity reqEntity = new StringEntity(encodeBody, "UTF-8");
            httpPost.setEntity(reqEntity);
            Long startTime = System.currentTimeMillis();
            try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    resultData = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
                }
                Long endTime = System.currentTimeMillis();
                log.warn("TITLE:{} 结束,localId:{}, apiCode:{}, url:{},requestParam:{},result:{} ",TITLE,localId,apiCode,realRequestUrl,requestParam,resultData.toJSONString());
                saveLog(localId,1,rid,"0",requestParam,realRequestUrl,header,statusCode,resultData,(endTime-startTime));
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("uMeng-智能时机任务创建请求失败，localId: %s，apiCode: %s", localId, apiCode), e);
        }
        return result.success().setDate(resultData);
    }

    @Override
    public Result deviceAdd(Long localId, String apiCode, String requestParam,Boolean isProxy) {
        Result result = new Result().failure();
        JSONObject resultData = null;

        String rid = UUID.randomUUID().toString();
        String bizId = marketingCommonConfig.getUMengBizInfoMap().get("bizId");
        String bizSecret = marketingCommonConfig.getUMengBizInfoMap().get("bizSecret");
        String encodeBody = UMengCryptoUtil.encryptBody(bizSecret, requestParam);
        String sign = UMengCryptoUtil.getRequestSign(bizId,bizSecret,encodeBody, rid);
        String realRequestUrl = marketingCommonConfig.getUMengUrlInfoMap().get("deviceAddUrl")+"="+sign;

        HttpClient httpClient = httpProxyClient.getHttpClientInner(isProxy);
        RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, socketTimeout, null);
        HttpPost httpPost = new HttpPost(realRequestUrl);
        httpPost.setConfig(requestConfig);
        log.warn("TITLE:{} 开始,localId:{}, apiCode:{},url:{} ",TITLE,localId,apiCode,realRequestUrl);
        try {
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("bizid", bizId);
            httpPost.setHeader("rid", rid);
            String header= "Content-Type: application/json; bizid:"+bizId+"; rid:"+rid;
            StringEntity reqEntity = new StringEntity(encodeBody, "UTF-8");
            httpPost.setEntity(reqEntity);
            Long startTime = System.currentTimeMillis();
            try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    resultData = null;
                }else {
                    resultData = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
                }
                Long endTime = System.currentTimeMillis();
                log.warn("TITLE:{} 结束,localId:{}, apiCode:{}, url:{},result:{} ",TITLE,localId,apiCode,realRequestUrl,resultData.toJSONString());
                saveLog(localId,2,rid,"0",requestParam,realRequestUrl,header,statusCode,resultData,(endTime-startTime));
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("uMeng-智能设备注册请求失败，localId: %s，apiCode: %s", localId, apiCode), e);
        }
        return result.success().setDate(resultData);
    }

    private void saveLog(Long localId, Integer requestType, String requestId, String eventType, String requestParam,
                        String url, String header, Integer httpCode, JSONObject resultData, Long exipreTime) {
        UMengInterfaceLog uMengInterfaceLog = new UMengInterfaceLog();
        uMengInterfaceLog.setLocalId(localId);
        uMengInterfaceLog.setRequestType(requestType);
        uMengInterfaceLog.setRequestId(requestId);
        uMengInterfaceLog.setEventType(eventType);
        uMengInterfaceLog.setRequestParam(requestParam);
        uMengInterfaceLog.setUrl(url);
        uMengInterfaceLog.setHeader(header);
        uMengInterfaceLog.setHttpCode(httpCode);
        if (resultData != null) {
            uMengInterfaceLog.setResult(resultData.toJSONString());
        }
        uMengInterfaceLog.setCallTime(1);
        Date now = new Date();
        uMengInterfaceLog.setCreateTime(now);
        uMengInterfaceLog.setUpdateTime(now);
        uMengInterfaceLog.setExpire(String.valueOf((exipreTime)));
        umengInterfaceLogMapper.insertSelective(uMengInterfaceLog);
    }




}
