package com.br.marketing.client.shunfeng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.shunfeng.input.BusinessListReq;
import com.br.marketing.client.shunfeng.input.BussinesInfoReqDTO;
import com.br.marketing.client.shunfeng.input.BussinessInfoReq;
import com.br.marketing.client.shunfeng.input.TokenReqDTO;
import com.br.marketing.client.shunfeng.output.BusinessListResponse;
import com.br.marketing.client.shunfeng.output.BussinesInfoReponse;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

/**
 * @Description 顺丰接口调用客户端
 * @Author zhen.Li1
 * @CreateTime 2024/10/28
 */
@Slf4j
@Service
public class ShunfengClient {

    @Value("${api.shunfeng.isProxy:false}")
    Boolean isProxy;

    @Value("${api.shunfeng.getTokenUrl:00}")
    private String getTokenUrl;

    @Value("${api.shunfeng.businessInfoUrl:00}")
    private String businessInfoUrl;

    @Value("${api.shunfeng.partnerID:00}")
    private String partnerID;

    @Value("${api.shunfeng.secret:00}")
    private String secret;

    @Autowired
    HttpProxyClient httpProxyClient;


    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<JSONObject> getToken() {
        HashMap<String, String> resMap = new HashMap<>();
        TokenReqDTO req = new TokenReqDTO();
        req.setPartnerID(partnerID);
        req.setSecret(secret);
        req.setGrantType("password");
        resMap = httpProxyClient.sendByCodeWithLog(req, getTokenUrl, isProxy,
                MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                JSON.toJSONString(req), true, true);

        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(),
                    "顺丰获取token接口异常-请求参数:" + JSON.toJSONString(req) + ";返回:" + JSON.toJSONString(resMap)));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        String resultCode = resultJson.getString("apiResultCode");
        if ("A1000".equals(resultCode)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultJson);
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(), "顺丰获取token接口异常，返回apiResultCode非A1000"));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
    }


    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<BussinesInfoReponse> getBussinessDetailInfo(BussinessInfoReq bussinessInfoReq, String token) {
        try {
            HashMap<String, String> resMap = new HashMap<>();
            BussinesInfoReqDTO bussinesInfoReqDTO = new BussinesInfoReqDTO();
            bussinesInfoReqDTO.setPartnerID(partnerID);
            bussinesInfoReqDTO.setServiceCode("COM_RECE_FEC_GET_COMPANY_PUBLIC_INFO");
            bussinesInfoReqDTO.setAccessToken(token);
            bussinesInfoReqDTO.setRequestID(UUID.randomUUID().toString());
            bussinesInfoReqDTO.setMsgData(JSON.toJSONString(bussinessInfoReq));
            bussinesInfoReqDTO.setTimestamp(System.currentTimeMillis());
            resMap = httpProxyClient.sendByCodeWithLog(bussinesInfoReqDTO, businessInfoUrl, isProxy,
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    JSON.toJSONString(bussinesInfoReqDTO), true, true);
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(),
                        "顺丰获取企业信息接口异常-请求参数:" + JSON.toJSONString(bussinesInfoReqDTO) + ";返回:" + JSON.toJSONString(resMap)));
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
            }
            String content = resMap.get("content");
            JSONObject resultJson = JSONObject.parseObject(content);
            String resultCode = resultJson.getString("apiResultCode");
            JSONObject resultData = JSON.parseObject(resultJson.getString("apiResultData"));
            String errorCode = resultData.getString("errorCode");
            BussinesInfoReponse bussinesInfoReponse = JSON.parseObject(resultData.getString("data"), new TypeReference<BussinesInfoReponse>() {
            }.getType());
            if ("A1000".equals(resultCode) && ("0".equals(errorCode))) {
                return new Result<BussinesInfoReponse>().setCode(ResultCode.SUCCESS.getValue()).setDate(bussinesInfoReponse);
            } else {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(),
                        "顺丰获取企业信息接口异常，返回apiResultCode 非A1000或 errorCode非0,返回结果=".concat(JSON.toJSONString(resMap))));
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
            }
        }catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(), "顺丰获取企业信息程序异常！"), ex);
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
    }


    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<BussinesInfoReponse> getBussinessList(BusinessListReq businessListReq, String token) {
        HashMap<String, String> resMap = new HashMap<>();
        BussinesInfoReqDTO bussinesInfoReqDTO = new BussinesInfoReqDTO();
        bussinesInfoReqDTO.setPartnerID(partnerID);
        bussinesInfoReqDTO.setServiceCode("COM_RECE_FEC_FIND_COMPANY_LOC_IND_INFO");
        bussinesInfoReqDTO.setAccessToken(token);
        bussinesInfoReqDTO.setRequestID(UUID.randomUUID().toString());
        bussinesInfoReqDTO.setMsgData(JSON.toJSONString(businessListReq));
        bussinesInfoReqDTO.setTimestamp(System.currentTimeMillis());
        resMap = httpProxyClient.sendByCodeWithLog(bussinesInfoReqDTO, businessInfoUrl, isProxy,
                MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                JSON.toJSONString(bussinesInfoReqDTO), true, true);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(),
                    "顺丰获取企业列表接口异常-请求参数:" + JSON.toJSONString(bussinesInfoReqDTO) + ";返回:" + JSON.toJSONString(resMap)));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        String resultCode = resultJson.getString("apiResultCode");
        JSONObject resultData = JSON.parseObject(resultJson.getString("apiResultData"));
        String errorCode = resultData.getString("errorCode");
        BusinessListResponse businessListResponse = JSON.parseObject(resultData.getString("data"), new TypeReference<BusinessListResponse>() {
        }.getType());
        if ("A1000".equals(resultCode) && ("0".equals(errorCode))) {
            return new Result<BussinesInfoReponse>().setCode(ResultCode.SUCCESS.getValue()).setDate(businessListResponse);
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(),
                    "顺丰获取企业列表接口异常，返回apiResultCode 非A1000或 errorCode非0"));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
    }


}
