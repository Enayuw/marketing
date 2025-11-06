package com.br.marketing.client;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * middleHeaven-三方营销数据回传客户端
 * @author zhiyong.zhang
 * @create 2025-10-17 19:45
 */
@Service
@Slf4j
public class MiddleHeavenAviatorScriptApiClient {

    @Resource
    private HttpProxyClient httpProxyClient;

    @RetryMethod(retryNowNum = 3, isOrNoDbRetry = false)
    public Result dealAviatorScriptRequest(String httpUrl,JSONObject requestJson,boolean isProxy) {
        Result result = new Result();
        try {
            Header[] headers = new Header[] {
                    new BasicHeader("Content-Type", "application/json"),
            };
            HashMap<String, String> resultMap = httpProxyClient.sendByCodeWithLogWithHeader(requestJson,httpUrl,isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,"",true,false,headers);
            log.warn("dealAviatorScriptRequest,httpUrl:{},isProxy:{},requestParam:{},result:{}",httpUrl,isProxy,
                    requestJson.toJSONString(),JSONObject.toJSONString(resultMap));
            String httpCode = resultMap.get("httpcode");
            String resultContentStr = resultMap.get("content");
            //重试判断:网络请求不成功,resultMap中content、httpCode为空
            if (StringUtils.isEmpty(httpCode) || StringUtils.isEmpty(resultContentStr)) {
                return result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage("网络请求返回数据httpCode、content为空");
            }
            //重试判断: httpCode 和业务状态码判断
            if (!httpCode.equals("200")) {
                return result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSONObject.toJSONString(resultMap));
            }
            //重试判断: 返回数据解析
            try{
                JSONObject resultData = JSONObject.parseObject(resultContentStr);
                boolean isSuccess = "000000".equals(resultData.getString("code"));
                result.setCode(isSuccess ? ResultCode.SUCCESS.getValue() : ResultCode.FAIL.getValue());
                if (!isSuccess) {
                    result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(resultContentStr);
                }
            }catch (Exception ex){
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINESMS_ERROR.getCode(), ex.getMessage()), ex);
                result.setCode(ResultCode.FAIL.getValue()).setMessage(resultContentStr);
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINESMS_ERROR.getCode(), ex.getMessage()), ex);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }

}
