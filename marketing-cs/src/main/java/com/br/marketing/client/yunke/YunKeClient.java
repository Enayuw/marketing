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
import com.google.common.collect.Lists;
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
    @Value("${api.yunKe.baseUrl:00}")
    private String url;
    @Value("${api.yunKe.appId:00}")
    private String appId;
    @Value("${api.yunKe.appKey:00}")
    private String appKey;
    @Value("${api.yunKe.encryptionType:00}")
    private String encryptionType;
    @Value("${api.yunKe.version:00}")
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
        HashMap<String, String> resMap = null;
        //云客接口挡板
        if (!marketingCommonConfig.getYunKeDeviceTypeApiSwitch()) {
            log.warn("云客机型获取job挡板打开,任务暂停");
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(buildResultDto());
        } else {
            resMap = httpProxyClient.sendByCodeWithLog(paramMap, url, true,
                    MediaType.APPLICATION_JSON_UTF8_VALUE, null, true, false);
        }
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YUNKE_SERVICEERROR.getCode(),
                    "云客机型获取接口请求异常!"));
            log.warn("调用云客api获取机型异常! 参数:{},结果:{}", JSONObject.toJSONString(paramMap),
                    JSONObject.toJSONString(resMap));
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(buildResultDto());
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if (code != 0) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YUNKE_SERVICEERROR.getCode(),
                    "云客机型获取接口返回错误!"));
            log.warn("云客机型获取接口返回错误结果:{}", content);
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(buildResultDto());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(JSON.parseObject(content,
                YunKeResponseDto.class));
    }

    YunKeResponseDto buildResultDto() {
        YunKeResponseDto yunKeResponseDto = new YunKeResponseDto();
        yunKeResponseDto.setCode("0");
        yunKeResponseDto.setData(Lists.newArrayList());
        return new YunKeResponseDto();
    }
}
