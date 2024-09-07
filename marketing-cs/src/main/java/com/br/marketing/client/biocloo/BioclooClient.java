package com.br.marketing.client.biocloo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.biocloo.input.BlackDataRequestDTO;
import com.br.marketing.client.biocloo.utils.AESUtil;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import java.util.HashMap;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.br.marketing.client.HttpProxyClient;

import lombok.extern.slf4j.Slf4j;

/**
 * 百可录客户端
 *
 * @author senyang.zheng
 * @date 2024/09/07
 */
@Service
@Slf4j
public class BioclooClient {

    @Value(value = "${api.biocloo.blackList}")
    private String blackListUrl;

    @Value(value = "${api.biocloo.aesKey}")
    private String aesKey;


    @Value("${api.biocloo.isProxy}")
    private Boolean isProxy;

    @Resource
    private HttpProxyClient httpProxyClient;

    private final static String TITLE = "【推送百可录数据】";

    @RetryMethod(retryNowNum = 3, isOrNoDbRetry = true)
    public Result pushBlackDataToBiocloo(BlackDataRequestDTO dto, Integer retry) {
        log.warn(TITLE + "加密前请求参数, dto{}", JSONObject.toJSONString(dto));
        String encryptData = AESUtil.encryptToBase64(JSONObject.toJSONString(dto), aesKey);
        JSONObject param = new JSONObject();
        param.put("apiCode", dto.getApiCode());
        param.put("encryptData", encryptData);
        long start = System.currentTimeMillis();
        log.warn(TITLE + "调度开始, requestParam{}", JSONObject.toJSONString(dto));
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(param, blackListUrl, isProxy, MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            JSONObject.toJSONString(dto), true, true);
        long end = System.currentTimeMillis();
        log.warn(TITLE + "调度结束, result:{}, 耗时:{}", resMap, end - start);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("调用百可录【黑名单】接口异常-请求参数:{};返回:{}", JSON.toJSONString(dto), JSON.toJSONString(resMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        String code = resultJson.getString("code");
        if ("000000".equals(code)) {
            log.warn("调用百可录【黑名单】接口，返回code为000000，请求正常");
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(content);
        } else {
            log.error("调用百可录【黑名单】接口异常，返回code非000000，最多重试三次");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }
}
