package com.br.marketing.client.biocloo;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import java.util.HashMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.biocloo.input.BlackDataDTO;
import com.br.marketing.client.biocloo.input.BlackDataRequestDTO;
import com.br.marketing.client.biocloo.input.BlackDataSoleDTO;
import com.br.marketing.client.biocloo.utils.AESUtil;
import com.br.marketing.common.annoation.DistributeLog;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import cn.hutool.core.collection.CollectionUtil;
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
    @Value(value = "${api.biocloo.blackList:'https://callcenter.biocloo.com.cn/api/middle/robotai/v1/process_data'}")
    private String blackListUrl;

    @Value("${api.biocloo.isProxy:true}")
    private Boolean isProxy;

    @Resource
    private HttpProxyClient httpProxyClient;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private final static String TITLE = "【推送百可录数据】";

    @RetryMethod(retryNowNum = 3, isOrNoDbRetry = false)
    @DistributeLog
    public Result pushBlackDataToBiocloo(BlackDataSoleDTO soleDTO, Integer retry) {
        BlackDataDTO dto = new BlackDataDTO();
        dto.setMethod("blackData");
        if (CollectionUtil.isEmpty(soleDTO.getData())) {
            log.warn("推送百可录去重后推送数据为0");
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        dto.setData(soleDTO.getData());
        dto.setApiCode(soleDTO.getApiCode());
        log.warn(TITLE + "加密前请求参数, dto{}", JSONObject.toJSONString(dto));
        JSONObject shuHeToBioclooAesKeyConfig = marketingCommonConfig.getShuHeToBioclooAesKeyConfig();
        String encryptData = AESUtil.encryptToBase64(JSONObject.toJSONString(dto), shuHeToBioclooAesKeyConfig.getString(dto.getApiCode()));
        BlackDataRequestDTO requestDTO = new BlackDataRequestDTO();
        requestDTO.setApiCode(dto.getApiCode());
        requestDTO.setJsonData(encryptData);
        long start = System.currentTimeMillis();
        log.warn(TITLE + "调度开始, requestParam{}", JSONObject.toJSONString(dto));
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(requestDTO, blackListUrl, isProxy,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE, JSONObject.toJSONString(dto), true, true);
        long end = System.currentTimeMillis();
        log.warn(TITLE + "调度结束, result:{}, 耗时:{}", resMap, end - start);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUHE_INTERFACEERROR.getCode(),
                String.format("请求参数:%s,返回:%s", JSON.toJSONString(dto), JSON.toJSONString(resMap)), "调用百可录【黑名单】接口异常"));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        String code = resultJson.getString("code");
        if ("000000".equals(code)) {
            log.warn("调用百可录【黑名单】接口，返回code为000000，请求正常");
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(content);
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUHE_INTERFACEERROR.getCode(), "返回code非000000，最多重试三次",
                    "调用百可录【黑名单】接口异常"));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

}
