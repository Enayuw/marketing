package com.br.marketing.client.halo;

import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.halo.input.ReqHaluoApiDTO;
import com.br.marketing.client.halo.send.PublicParamsConstants;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class HaluoAiApiServiceClient {
    @Value("${api.halo.ai.callbackUrl:0}")
    private String haloCallbackUrl;

    @Value("${api.halo.ai.appKey:0}")
    private String haloCallbackAppKey;

    @Value("${api.halo.ai.secret:0}")
    private String haloCallbackSecret;

    @Value("${api.halo.ai.isProxy:false}")
    private boolean isProxy;

    @Autowired
    HttpProxyClient httpProxyClient;

    /**
     * 哈啰openApi接口统一调用方法
     *
     * @param reqHaluoApiDTO
     * @return Result
     */
    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<String> postHaluoCallbackApi(ReqHaluoApiDTO reqHaluoApiDTO) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Map paramsMap = new HashMap();
        paramsMap.put(PublicParamsConstants.APPKEY, haloCallbackAppKey);
        paramsMap.put(PublicParamsConstants.METHOD, reqHaluoApiDTO.getMethod());
        paramsMap.put(PublicParamsConstants.TIMESTAMP, timestamp.toString());
        paramsMap.put("token", null);
        paramsMap.put("data", reqHaluoApiDTO.getData());
        paramsMap.put("channelNo", "BR");
        String sign = EncryptUtil.signTopRequest(paramsMap, haloCallbackSecret);
        paramsMap.put(PublicParamsConstants.SIGN, sign);
        HashMap<String, String> response = httpProxyClient.sendByCode(paramsMap
                , haloCallbackUrl
                , isProxy
                , MediaType.APPLICATION_JSON_UTF8_VALUE
                , null);
        String code = response.get("httpcode");
        if ("200".equals(code)) {
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(response.get("content"));
        } else {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
    }

}
