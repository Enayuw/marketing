package com.br.marketing.client.tongcheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.Impl.MockConfigServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @Description DiDiClient
 * @Author hong.chen
 * @CreateTime 2023/04/23
 */

@Service
@Slf4j
public class TongChengClient {
    @Value("${api.tongcheng.address:0}")
    String reachUrl;
    @Value("${api.tongcheng.isProxy:0}")
    Boolean isProxy;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    MockConfigServiceImpl mockConfigService;

    @Autowired
    HttpProxyClient httpProxyClient;

    /**
     * 同程不运营名单推送客户接口
     */
    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result pushToTongChengCustomer(JSONObject jsonObject
            , Integer retry) {
        HashMap<String, String> resMap = new HashMap<>();
        // 获取挡板开关
        HashMap<String, Object> mock = marketingCommonConfig.getTongChengUndoMock();
        if (mock.get("switch") == Boolean.TRUE) {
            JSONObject mockJson = new JSONObject();
            mockJson.put("code", mock.get("code"));
            mockJson.put("message", "处理成功");
            resMap.put("content", JSON.toJSONString(mockJson));
            resMap.put("httpcode", mock.get("httpcode").toString());
        } else {
            resMap = httpProxyClient.sendByCodeWithLog(jsonObject, reachUrl, isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,
                    JSON.toJSONString(jsonObject), true, true);
        }

        // 1.httpcode不为200，需要重试
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("调用同程不运营名单接口异常-请求参数taskId:{};返回:{}", JSON.toJSONString(jsonObject.get("taskId")), JSON.toJSONString(resMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");

        if (code == 0) {
            log.warn("调用同程不运营名单接口，返回code为0，请求正常");
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(content);
        }

        if (code == 1002 || code == 1003 || code == 1004) {
            log.error("调用同程不运营名单接口，返回code非0。立即重试，最多重试三次");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        log.error("调用同程不运营名单接口，返回code非0且非重试code。不会重试");
        return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(content);
    }
}
