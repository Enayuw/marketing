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
public class HaluoApiServiceClient {
    @Value("${api.halo.openUrl:00}")
    private String haloOpenUrl;

    @Value("${api.halo.appKey:00}")
    private String haloAppKey;

    @Value("${api.halo.secret:00}")
    private String haloSecret;

    @Value("${api.halo.isProxy:false}")
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
    public Result<String> postHaluoOpenApi(ReqHaluoApiDTO reqHaluoApiDTO) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Map paramsMap = new HashMap();
        paramsMap.put(PublicParamsConstants.APPKEY, haloAppKey);
        paramsMap.put(PublicParamsConstants.METHOD, reqHaluoApiDTO.getMethod());
        paramsMap.put(PublicParamsConstants.TIMESTAMP, timestamp.toString());
        paramsMap.put("token", null);
        paramsMap.put("data", reqHaluoApiDTO.getData());
        paramsMap.put("channelNo", "BR");
        String sign = EncryptUtil.signTopRequest(paramsMap, haloSecret);
        paramsMap.put(PublicParamsConstants.SIGN, sign);
        HashMap<String, String> response = httpProxyClient.sendByCode(paramsMap
                , haloOpenUrl
                , isProxy
                , MediaType.APPLICATION_JSON_UTF8_VALUE
                , null);
        String code = response.get("httpcode");
        if ("200".equals(code)) {
            JSONObject jsonResult = JSONObject.parseObject(response.get("content"));
            JSONObject jsonData = JSONObject.parseObject(jsonResult.getString("data"));
            if(Objects.isNull(jsonData)){
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate(jsonResult.toJSONString());
            }
            //重试
            if (!jsonData.getBoolean("success")) {
                return new Result<String>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(jsonResult.toJSONString());
            }
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(jsonResult.toJSONString());
        } else {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
    }

}
