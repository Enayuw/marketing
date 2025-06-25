package com.br.marketing.client.sanliuling;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    private final static String TITLE = "【360-】";

    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result batchTrafficData(SanLiuLingTrafficReq req) {
        Result result = new Result();
        Map<String, String> httpResponseMap = new HashMap<>();
        try {
            // 获取挡板开关
            HashMap<String, Object> mock = marketingCommonConfig.getSanLiuLingTrafficMock();
            if ("1".equals(mock.get("switch"))) {
                log.warn(TITLE + "mock开关开启");
                Integer code = (Integer) mock.get("code");
                String message = (String) mock.get("message");
                if (!Objects.equals(ResultCode.SUCCESS.getValue(), code)) {
                    result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                    result.setMessage(message);
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULING_SERVICEERROR.getCode()
                            , TITLE + "360mock失败，失败原因：" + mock));
                    return result;
                }
                result.setCode(ResultCode.SUCCESS.getValue());
                result.setDate(mock.get("data"));
                log.warn(TITLE + "流量业务营销result: {}", JSONObject.toJSON(result));
                return result;
            }
            httpResponseMap = httpProxyClient.sendByCodeWithLog(req, url, isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,
                    JSON.toJSONString(req), true, true);

            if (String.valueOf(HttpStatus.SC_OK).equals(httpResponseMap.get(CODE_KEY))) {
                result.setDate(httpResponseMap.get(CONTENT_KEY));
                result.setCode(ResultCode.SUCCESS.getValue());
                result.setMessage("成功");
                return result;
            }

            if (httpResponseMap.get(CONTENT_KEY) != null) {
                String content = httpResponseMap.get(CONTENT_KEY);
                JSONObject resultJson = JSONObject.parseObject(content);
                String code = resultJson.getString("code");
                if (!"200".equals(code)) {
                    result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULING_SERVICEERROR.getCode()
                            , TITLE + "流量业务营销，失败原因：" + httpResponseMap));
                    return result;
                }
            }
        } catch (Exception e) {
            String eMsg = TITLE + "流量业务营销接口异常:" + e.getMessage();
            result.setMessage(eMsg);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULING_SERVICEERROR.getCode(), eMsg));
        }
        result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        return result;
    }


}
