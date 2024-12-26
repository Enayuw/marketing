package com.br.marketing.client.smy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.smy.input.SmyCommReqDto;
import com.br.marketing.client.smy.output.SmyCommRespDto;
import com.br.marketing.client.smy.util.MarketingSign4SmyUtil;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Description 萨摩耶 client
 * @Author bin.li1
 * @CreateTime 2024-12-19
 */
@Slf4j
@Component
public class SmyClient {
    @Value("${api.smy.isProxy:false}")
    private Boolean isProxy;
    @Value("${api.smy.modelTagUrl:0}")
    private String modelTagUrl;
    @Value("${api.smy.merchantNo:G840120241220}")
    private String merchantNo;
    @Value("${api.smy.smyPublicKey:0}")
    private String smyPublicKey;
    @Value("${api.smy.marketPubklicKey:0}")
    private String marketPubklicKey;
    @Value("${api.smy.marketPrivateKey:0}")
    private String marketPrivateKey;
    @Resource
    HttpProxyClient httpProxyClient;
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result sendSmyBlackList(SmyCommReqDto commReqDto) {
        try{
            commReqDto.setMerchantNo(merchantNo);
            commReqDto.setVersion("4.0");
            if(!MarketingSign4SmyUtil.signSmyRequest(marketPrivateKey,smyPublicKey,commReqDto)){
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            Map<String, String> resMap = httpProxyClient.sendByCodeWithLog(commReqDto
                    , modelTagUrl, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, "", true, false);
            if(log.isInfoEnabled()){
                log.info("调用萨摩耶营销模型标识上传接口推送黑名单响应数据：{}",resMap);
            }
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SMY_INTERFACEERROR.getCode(),
                        String.format("萨摩耶黑名单推送请求失败-请求参数:%s;返回:%s", JSON.toJSONString(commReqDto),JSON.toJSONString(resMap))));
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            SmyCommRespDto commRespDto = JSON.parseObject(resMap.get("content"), SmyCommRespDto.class);
            if(!MarketingSign4SmyUtil.verifySignSmyResponse(marketPrivateKey,smyPublicKey,commRespDto)){
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SMY_SERVICEERROR.getCode(),
                        String.format("萨摩耶黑名单推送响应结果验签失败-请求参数:%s;返回:%s", JSON.toJSONString(commReqDto),JSON.toJSONString(resMap))));
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            JSONObject respJson = JSON.parseObject(commRespDto.getBizContent());
            if(respJson.containsKey("code") && !"10000".equals(respJson.getString("code"))){
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SMY_SERVICEERROR.getCode(),
                        String.format("萨摩耶黑名单推送失败-请求参数:%s;返回:%s", JSON.toJSONString(commReqDto),JSON.toJSONString(respJson))));
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }catch (Exception ex){
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SMY_SERVICEERROR.getCode(),
                    String.format("萨摩耶黑名单推送处理异常-请求参数:%s;异常:%s;", JSON.toJSONString(commReqDto), ex.getMessage())), ex);
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
    }
}
