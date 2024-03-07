package com.br.marketing.client.dewu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.xiecheng.intput.AdReqDTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class DewuClient {


    @Autowired
    HttpProxyClient httpProxyClient;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @RetryMethod(retryNowNum = 3)
    public Result pushCollidingData(List<String> mobileList) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appId",marketingCommonConfig.getDeWuAppId());
        jsonObject.put("mobile",mobileList);

        HashMap<String, String> resMap =
                httpProxyClient.sendByCodeWithLog(jsonObject,
                        marketingCommonConfig.getDeWuCollidingUrl(),
                        true,
                        MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(jsonObject),
                        true,
                        false);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("得物撞库接口异常:{}", JSON.toJSONString(jsonObject));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");

        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if (code == 200) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(content);
        }
        log.error("得物撞库接口异常:{}", JSON.toJSONString(jsonObject));
        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
    }

}
