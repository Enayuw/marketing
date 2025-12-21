package com.br.marketing.client.didi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.aspect.Mockable;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.didi.input.DiDiSmsRequestTO;
import com.br.marketing.client.didi.input.v5.DiDiV5CollidingRequestDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.constants.MockConstants;
import java.util.HashMap;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DiDiV5Client {

    @Value("${api.didi.collidingUrl:https://admarketing-manhattan.xiaojukeji.com/crow/collision/mediaName}")
    private String collidingUrl;

    @Value("${api.didi.collidingUrl:https://admarketing-manhattan.xiaojukeji.com/crow/user/success/mediaName}")
    private String callbackSuccessUrl;

    @Value("${api.didi.isProxy:false}")
    private Boolean isProxy;
    @Resource
    private HttpProxyClient httpProxyClient;


    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    @Mockable(mockName = MockConstants.TEST_DIDI_V5_COLLIDING_DATA_RETURN)
    public Result<String> colliding(String mediaName, DiDiV5CollidingRequestDTO requestDTO) {
        collidingUrl = collidingUrl.replace("mediaName", mediaName);
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(requestDTO, collidingUrl, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE,
                JSON.toJSONString(requestDTO), true, false);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(JSONObject.toJSONString(resMap));
    }

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    @Mockable(mockName = MockConstants.TEST_DIDI_V5_COLLIDING_DATA_RETURN)
    public Result<String> callbackSuccess(String mediaName, DiDiSmsRequestTO smsRequestTO) {
        callbackSuccessUrl = callbackSuccessUrl.replace("mediaName", mediaName);
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(smsRequestTO, collidingUrl, isProxy, MediaType.APPLICATION_JSON_VALUE,
                JSON.toJSONString(smsRequestTO), true, false);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(JSONObject.toJSONString(resMap));
    }
}
