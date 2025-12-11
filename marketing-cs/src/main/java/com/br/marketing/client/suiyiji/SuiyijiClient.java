package com.br.marketing.client.suiyiji;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.aspect.Mockable;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.constants.MockConstants;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 随意记 client
 * @Author zhen.li1
 * @CreateTime 2025-12-04
 */
@Slf4j
@Component
public class SuiyijiClient {

    @Value("${api.syj.getBlackUrl:https://partner.ppdai.com/blackList/complaint/bairong}")
    private String getBlackUrl;

    @Value("${api.syj.originalUrl:https://partner.yhyunjin.com/yjPart/bairong}")
    private String originalUrl;

    @Value("${api.syj.blackUrl:https://partner.yhyunjin.com/yjPart/blackList/insertUnsubscribeData/bairong}")
    private String blackUrl;

    @Value("${api.syj.isProxy:true}")
    private Boolean isProxy;


    @Value("${api.syj.brPrivateKey:0}")
    private String brPrivateKey;

    @Resource
    private HttpProxyClient httpProxyClient;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @RetryMethod(retryNowNum = 2)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    @Mockable(mockName = MockConstants.SUIYIJI_QUERY_BLACK)
    public Result<String> getBlackList() {
        try {
            HashMap<String, String> resMap = httpProxyClient.getWithLog(getBlackUrl, isProxy, null);
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUIYIJI_SERVICE_ERROR.getCode()
                        , "随意记获取黑名单接口异常-请求url:" + getBlackUrl + ";返回:" + JSON.toJSONString(resMap)));
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
            }
            String content = resMap.get("content");
            JSONObject resultJson = JSONObject.parseObject(content);
            String returncode = resultJson.getString("code");
            String data = resultJson.getString("data");
            if ("0".equals(returncode)) {
                if (StringUtils.isNotEmpty(data)) {
                    //RSA解密
                    if (StringUtils.isNotEmpty(marketingCommonConfig.getSuiyijiBlackBrPrivateKey())) {
                        brPrivateKey = marketingCommonConfig.getSuiyijiBlackBrPrivateKey();
                    }
                    log.warn("调用随意记获取黑名单解密，data={},brPrivateKey={}", data, brPrivateKey);
                    String decodeStr = SuiyijiRSAUtil.decryptByPrivateKey(data, brPrivateKey);
                    //String decodeStr = "[\"15711399935\",\"1843452345\"]";
                    return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(decodeStr);
                } else {
                    log.error("随意记获取黑名单返回data为空");
                    return new Result().setCode(ResultCode.FAIL.getValue());
                }
            } else {
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUIYIJI_SERVICE_ERROR.getCode(), e.getMessage()
                    , "随意记获取黑名单接口异常"));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

    }

    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    @Mockable(mockName = MockConstants.SUIYIJI_ORIGINAL)
    public Result<String> originalApi(Object reqMap) {
        try {
            Map<String, String> responseMap = httpProxyClient.sendByCodeWithLog(
                    reqMap,
                    originalUrl,
                    isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,
                    com.alibaba.fastjson2.JSON.toJSONString(reqMap),
                    true,
                    true
            );

            if (!"200".equals(responseMap.get("httpcode")) || StringUtils.isBlank(responseMap.get("content"))) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUIYIJI_SERVICE_ERROR.getCode()
                        , "随忆记用户信息撞库接口异常-请求url:" + originalUrl + ";返回:" + JSON.toJSONString(responseMap)));
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(responseMap));
            }

            String content = responseMap.get("content");
            JSONObject jsonObject = JSON.parseObject(content);
            String code = jsonObject.getString("code");
            String result = jsonObject.getString("result");
            String message = jsonObject.getString("message");

            // 只有code = 0 && result = 1的情况下queryStatus才是3
            if ("0".equals(code) && "1".equals(result)) {
                return new Result<>().success().setMessage(message);
            } else {
                // 其他情况（code!=0 或 result!=1），queryStatus=2
                return new Result<>().failure().setMessage(message);
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUIYIJI_SERVICE_ERROR.getCode(), e.getMessage()
                    , "随忆记用户信息撞库接口异常"));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage("接口异常");
        }
    }

    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    @Mockable(mockName = MockConstants.SUIYIJI_BLACK)
    public Result<Integer> blackApi(Object reqMap) {
        try {
            Map<String, String> responseMap = httpProxyClient.sendByCodeWithLog(
                    reqMap,
                    blackUrl,
                    isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,
                    com.alibaba.fastjson2.JSON.toJSONString(reqMap),
                    true,
                    true
            );

            if (!"200".equals(responseMap.get("httpcode")) || StringUtils.isBlank(responseMap.get("content"))) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUIYIJI_SERVICE_ERROR.getCode()
                        , "随忆记黑名单接口异常-请求url:" + originalUrl + ";返回:" + JSON.toJSONString(responseMap)));
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(responseMap));
            }

            String content = responseMap.get("content");
            JSONObject jsonObject = JSON.parseObject(content);
            Integer code = jsonObject.getInteger("code");
            Integer succNum = jsonObject.getInteger("succNum");
            String msg = jsonObject.getString("msg");

            // code: 0-调用成功，-1-系统异常
            if (code == 0) {
                return new Result<>().success().setDate(succNum).setMessage(msg);
            } else {
                return new Result<>().failure().setDate(succNum).setMessage(msg);
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUIYIJI_SERVICE_ERROR.getCode(), e.getMessage()
                    , "随意记获取黑名单接口异常"));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage("接口异常");
        }
    }
}
