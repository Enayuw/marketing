package com.br.marketing.client.yunke;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.yunke.output.YunKeResponseDto;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author peng.kang
 * @description: 云客客户端请求
 * @date 2025/5/25 20:29
 */
@Service
@Slf4j
public class YunKeClient {
    @Value("${api.yunKe.baseUrl}")
    private String url;
    @Value("${api.yunKe.appId}")
    private String appId;
    @Value("${api.yunKe.appKey}")
    private String appKey;
    @Value("${api.yunKe.encryptionType}")
    private String encryptionType;
    @Value("${api.yunKe.version}")
    private String version;
    @Autowired
    HttpProxyClient httpProxyClient;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<YunKeResponseDto> getYunKeDeviceType(List<String> cells) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("appId", appId);
        paramMap.put("timestamp", System.currentTimeMillis() + "");
        paramMap.put("signType", encryptionType);
        paramMap.put("version", version);
        String sign = SignUtils.yunKeSign(paramMap, appKey);
        paramMap.put("sign", sign);
        paramMap.put("checkData", cells);
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(paramMap, url, false,
                MediaType.APPLICATION_JSON_UTF8_VALUE, null, true, false);

        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YUNKE_SERVICEERROR.getCode(),
                    "云客机型获取接口请求异常!"));
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if (code != 0) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YUNKE_SERVICEERROR.getCode(),
                    "云客机型获取接口返回错误!"));
            log.warn("云客机型获取接口返回错误结果:{}", content);
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(null));
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(JSON.parseObject(content,
                YunKeResponseDto.class));
    }
}
