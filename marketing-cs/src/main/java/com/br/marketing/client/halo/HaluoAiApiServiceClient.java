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
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class HaluoAiApiServiceClient {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

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
        Map paramsMap = new HashMap();
        paramsMap.put("data", reqHaluoApiDTO.getData());
        String haloCallbackUrl = marketingCommonConfig.getHaloAiCallbackConfig().getString("haloCallbackUrl");
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
