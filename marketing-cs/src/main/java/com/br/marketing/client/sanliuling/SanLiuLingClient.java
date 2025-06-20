package com.br.marketing.client.sanliuling;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName SanLiuLingClient
 * @Description 360相关接口
 * @Author kongbx
 * @Date 2025/6/20 16:17
 */
@Component
@Slf4j
public class SanLiuLingClient {

    @Value("${api.sanliuling.batch:0}")
    private String url;

    @Value("${api.sanliuling.isProxy:true}")
    private boolean isProxy;

    @Resource
    private HttpProxyClient httpProxyClient;

    private static final String CODE_KEY = "httpcode";
    private static final String CONTENT_KEY = "content";


    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result batchTrafficData(SanLiuLingTrafficReq req) {
        Result result = new Result();
        try {
            Map<String, String> httpResponseMap = httpProxyClient.sendByCodeWithLog(req, url, isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,
                    JSON.toJSONString(req), true, true);

            if (String.valueOf(HttpStatus.SC_OK).equals(httpResponseMap.get(CODE_KEY))) {
                result.setDate(httpResponseMap.get(CONTENT_KEY));
                result.setCode(ResultCode.SUCCESS.getValue());
                result.setMessage("");
                return result;
            }

            if (httpResponseMap.get(CONTENT_KEY) != null) {
                String content = httpResponseMap.get(CONTENT_KEY);
                JSONObject resultJson = JSONObject.parseObject(content);
                String code = resultJson.getString("code");
                if (!"200".equals(code)) {
                    result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                    return result;
                }
            }

        } catch (Exception e) {
            String eMsg = "360流量业务营销接口异常:" + e.getMessage();
            log.error(eMsg, e);
            result.setMessage(eMsg);
        }
        result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        return result;
    }


}
