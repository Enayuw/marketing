package com.br.marketing.service.Impl.umeng;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.UMengCryptoUtil;
import com.br.marketing.entity.UMengInterfaceLog;
import com.br.marketing.mapper.UMengInterfaceLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
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
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private HttpProxyClient httpProxyClient;

    private final int socketTimeout = 30000;

    @Override
    public Result createTimingTask(Long localId,String apiCode, String requestParam,Boolean isProxy) {
        Result result = new Result();
        JSONObject resultData = new JSONObject();
        String rid = UUID.randomUUID().toString();
        String bizId = marketingCommonConfig.getUMengBizInfoMap().get("bizId");
        String bizSecret = marketingCommonConfig.getUMengBizInfoMap().get("bizSecret");
        String encodeBody = UMengCryptoUtil.encryptBody(bizSecret, requestParam);
        String sign = UMengCryptoUtil.getRequestSign(bizId,bizSecret,encodeBody, rid);
        String realRequestUrl = marketingCommonConfig.getUMengUrlInfoMap().get("timingTaskUrl")+"="+sign;

        log.warn("TITLE:{} 开始,localId:{}, apiCode:{},url:{} ",TITLE,localId,apiCode,realRequestUrl);
        try {
            Header[] headers = new Header[] {
                    new BasicHeader("Content-Type", "application/json"),
                    new BasicHeader("bizid", bizId),
                    new BasicHeader("rid", rid),
            };
            HashMap<String, String> resultMap = httpProxyClient.sendByCodeWithLogWithHeader(encodeBody,realRequestUrl,true,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,"",true,false,headers);
            if (resultMap!=null && resultMap.get("httpcode").equals("200")) {
                String resContent = resultMap.get("content");
                resultData = JSONObject.parseObject(resContent);
            }else {
                return new Result().failure();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("uMeng-智能时机任务创建请求失败，localId: %s，apiCode: %s", localId, apiCode), e);
        }
        log.warn("TITLE:{} 结束,localId:{}, apiCode:{}, url:{},result:{} ",TITLE,localId,apiCode,realRequestUrl,resultData==null?"":resultData.toJSONString());
        return result.success().setDate(resultData);
    }

    @Override
    public Result deviceAdd(Long localId, String apiCode, String requestParam,Boolean isProxy) {
        Result result = new Result();
        JSONObject resultData = new JSONObject();
        String rid = UUID.randomUUID().toString();
        String bizId = marketingCommonConfig.getUMengBizInfoMap().get("bizId");
        String bizSecret = marketingCommonConfig.getUMengBizInfoMap().get("bizSecret");
        String encodeBody = UMengCryptoUtil.encryptBody(bizSecret, requestParam);
        String sign = UMengCryptoUtil.getRequestSign(bizId,bizSecret,encodeBody, rid);
        String realRequestUrl = marketingCommonConfig.getUMengUrlInfoMap().get("deviceAddUrl")+"="+sign;
        log.warn("TITLE:{} 开始,localId:{}, apiCode:{},url:{} ",TITLE,localId,apiCode,realRequestUrl);
        try {
            Header[] headers = new Header[] {
                    new BasicHeader("Content-Type", "application/json"),
                    new BasicHeader("bizid", bizId),
                    new BasicHeader("rid", rid),
            };
            HashMap<String, String> resultMap = httpProxyClient.sendByCodeWithLogWithHeader(encodeBody,realRequestUrl,true,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,"",true,false,headers);
            if (resultMap!=null && resultMap.get("httpcode").equals("200")) {
                String resContent = resultMap.get("content");
                resultData = JSONObject.parseObject(resContent);
            }else {
                return new Result().failure();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("uMeng-智能设备注册请求失败，localId: %s，apiCode: %s", localId, apiCode), e);
        }
        log.warn("TITLE:{} 结束,localId:{}, apiCode:{}, url:{},result:{} ",TITLE,localId,apiCode,realRequestUrl,resultData==null?"":resultData.toJSONString());
        return result.success().setDate(resultData);
    }




}
