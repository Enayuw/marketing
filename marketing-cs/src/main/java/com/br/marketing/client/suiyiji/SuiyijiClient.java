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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;

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

    @Value("${api.syj.isProxy:true}")
    private Boolean isProxy;


    @Value("${api.syj.brPrivateKey:0}")
    private String brPrivateKey;

    @Resource
    private HttpProxyClient httpProxyClient;

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
            //String data = resultJson.getString("data");
            String data = "111";
            if ("0".equals(returncode)) {
                if (StringUtils.isNotEmpty(data)) {
                    //RSA解密
                    //String decodeStr = SuiyijiRSAUtil.decryptByPrivateKey(data, brPrivateKey);
                    String decodeStr = "[\"15711399935\",\"1843452345\"]";

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
}
