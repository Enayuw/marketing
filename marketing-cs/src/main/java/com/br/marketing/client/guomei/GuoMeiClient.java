package com.br.marketing.client.guomei;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.guomei.result.GmMarketingResultCallBackRequest;
import com.br.marketing.client.guomei.userdata.GmUserDataCallBackRequest;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 国美客户端
 *
 * @author Hua Qiang
 * @date 2024-08-20 14:06
 */
@Component
@Slf4j
public class GuoMeiClient {

    @Value("${api.guomei.userDataCallBack.url:https://united-umg-t1.gomefinance.com.cn/umg/api/v1/agencyOperation/pushDataCallback}")
    private String pushDataCallbackUrl;

    @Value("${api.guomei.userDataCallBack.url:https://united-umg-t1.gomefinance.com.cn/umg/api/v1/agencyOperation/pushResultCallback}")
    private String pushResultCallbackUrl;

    @Value("${api.guomei.isProxy:false}")
    private boolean isProxy;

    @Resource
    private HttpProxyClient httpProxyClient;

    private static final String HTTP_CODE = "200";

    /**
     * 2024-08-20 19:38
     * 用户数据回传接口：（所有接收到的用户数据）
     * 1：批量推送：每次最多 1000 条
     * 2：用户为 0 的情况下，不需要调用接口
     * 3：如果有重推需保证 requestId 不变
     *
     * @param userDataCallBackRequest 用户数据
     * @return GmCallBackResponse
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public <T> Result<GmCallBackResponse<T>> sendUserDataCallBack(GmUserDataCallBackRequest userDataCallBackRequest, Class<T> responseClass) {
        Map<String, String> map = httpProxyClient.sendByCodeZw(userDataCallBackRequest
                , pushDataCallbackUrl, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, "");
        return getResponse(map, pushDataCallbackUrl, responseClass);
    }

    /**
     * 2024-08-20 19:38
     * <p>
     * 营销结果数据回传接口
     * 1：批量推送：每次 1000 条
     * 2：如果有重推需保证 requestId 不变
     *
     * @return
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public <T> Result<GmCallBackResponse<T>> sendMarketingResultCallBack(GmMarketingResultCallBackRequest marketingResultCallBackRequest
            , Class<T> responseClass) {
        Map<String, String> map = httpProxyClient.sendByCodeZw(marketingResultCallBackRequest
                , pushResultCallbackUrl, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, "");
        return getResponse(map, pushResultCallbackUrl, responseClass);
    }

    /**
     * 2024-08-20 21:05
     * 获取响应数据
     *
     * @param map           响应信息
     * @param url           请求地址
     * @param responseClass 响应类型，不支持继承（实现）类的泛型
     * @return httpcode 非正常时返回null
     */
    private static <T> Result<GmCallBackResponse<T>> getResponse(Map<String, String> map, String url, Class<T> responseClass) {
        Result<GmCallBackResponse<T>> result = new Result<>();
        try {
            String httpCode = map.getOrDefault("httpcode", "");
            if (HTTP_CODE.equals(httpCode)) {
                String respStr = map.getOrDefault("content", "");
                GmCallBackResponse<T> gmCallBackResponse = JSON.parseObject(respStr, new TypeReference<GmCallBackResponse<T>>(responseClass) {
                });
                result.setDate(gmCallBackResponse);
                result.setMessage(gmCallBackResponse.getMsg());
                result.setCode(ResultCode.SUCCESS.getValue());
            } else {
                // 网络非200客户要求需要重试，且重试时requestId 不更新
                result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
        } catch (Exception e) {
            result.setCode(ResultCode.FAIL.getValue());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.GUOMEI_INTERFACEERROR.getCode()
                    , url + "\n" + e.getMessage(), "国美回调接口响应内容解析失败"), e.getMessage());
        }
        return result;
    }

}
