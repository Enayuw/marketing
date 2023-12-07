package com.br.marketing.client.tongcheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.util.Map;

/**
 * @Description DiDiClient
 * @Author hong.chen
 * @CreateTime 2023/04/23
 */

@Service
@Slf4j
public class TongChengClient {
    @Value("${api.didi.smsUrl:https://admarketing-manhattan.xiaojukeji.com/crow/collision/bairong}")
    String smsUrl;
    @Value("${api.didi.reachUrl:https://admarketing-manhattan.xiaojukeji.com/crow/user/success/bairong}")
    String reachUrl;
    @Value("${api.didi.jmassSUrl:https://admarketing-manhattan.xiaojukeji.com/model/sample/bairong}")
    String jmassSUrl;
    @Value("${api.didi.failedUrl:https://admarketing-manhattan.xiaojukeji.com/crow/faileduser/mediaName}")
    String failUserUrl;

    @Value("${api.didi.token:DK&SgWl!fZ%WVSXe}")
    String token;

    @Value("${api.didi.scas:0001}")
    String scas;

    @Value("${api.didi.channelId:3140738836439875}")
    String channelId;

    @Value("#{${api.didi.channelIdMap:{bairong:'3140738836439875',bairongA:'3140738898634899'}}}")
    private Map<String, String> channelIdMap;

    @Value("${api.didi.isProxy:false}")
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
    @RetryMethod(retryNowNum = 2, isOrNoDbRetry = true)
    public Result pushToTongChengCustomer(JSONObject jsonObject
            , Integer retry) {
        HashMap<String, String> resMap = new HashMap<>();
        resMap = httpProxyClient.sendByCodeWithLog(jsonObject, reachUrl, isProxy,
                MediaType.APPLICATION_JSON_UTF8_VALUE,
                JSON.toJSONString(jsonObject), false, false);

        // 1.httpcode不为200，需要重试
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
//            if (!islogs.get(1)) {
//                log.error("调用滴滴短信流量接口异常-请求参数:{};返回:{}", JSON.toJSONString(smsReqVO), JSON.toJSONString(resMap));
//            }
            log.error("调用滴滴短信流量接口异常-请求参数:{};返回:{}", JSON.toJSONString(jsonObject), JSON.toJSONString(resMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(jsonObject);
    }


}
