package com.br.marketing.client;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

/**发送邮件客户端
 * @author 10400
 * @create 2017-06-27 13:33
 */
@Service
@Slf4j
public class HaloCallBackDataApiClient {
    @Value("${api.halo.openUrl:00}")
    private String haloCallBackUrl;

    @Resource
    private HttpProxyClient httpProxyClient;

    public Result dealMarketingCallBack(String apiCode,JSONObject requestJson) {
        Result result = new Result();
        try {
            Header[] headers = new Header[] {
                    new BasicHeader("Content-Type", "application/json"),
            };
            HashMap<String, String> resultMap = httpProxyClient.sendByCodeWithLogWithHeader(requestJson,haloCallBackUrl,true,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,"",true,false,headers);
            log.warn("dealMarketingCallBack,apiCode:{},requestParam:{},result:{}",apiCode,
                    requestJson.toJSONString(),JSONObject.toJSONString(resultMap));
            String resultContentStr = resultMap.get("content");
            if (!resultMap.get("httpcode").equals("200")) {
                return result.setCode(ResultCode.FAIL.getValue()).setMessage(resultContentStr);
            }
            JSONObject resultData = JSONObject.parseObject(resultContentStr);
            boolean isSuccess = "10000".equals(resultData.getString("code"));
            result.setCode(isSuccess ? ResultCode.SUCCESS.getValue() : ResultCode.FAIL.getValue());
            if (!isSuccess) {
                result.setMessage(resultContentStr);
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.HALUO_CALLBACK_DATA_INTERFACEERROR.getCode(), ex.getMessage()), ex);
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
        return result;
    }

}
