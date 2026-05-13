package com.br.marketing.client.mock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName MarketingMockApiService
 * @Description 查询Mock挡板配置信息
 * @Author kongbx
 * @Date 2025/9/25 16:29
 */
@Service
@Slf4j
public class MarketingMockApiService {
    @Autowired
    RestTemplate restTemplate;

    @Qualifier("interfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor interfaceLogDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;

    @Value("${api.mock.redisUrl:00}")
    String redisUrl;

    /**
     * 查询Mock挡板配置信息
     *
     * @param cacheKey redis key
     * @return Result<String>
     */
    @RetryMethod(retryNowNum = 2)
    public Result<String> queryMockConfig(String cacheKey) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("cacheKey", cacheKey);

            ThirdApiResultTransfer result = new ApiCallerUtil(restTemplate, null, interfaceLogDbpool)
                    .setUrl(redisUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(params)
                    .setEncode(Boolean.TRUE)
                    .postTransferStr();
            if (Integer.valueOf(200).equals(result.getHttpCode())) {
                JSONObject jsonObject = JSON.parseObject(result.getResult());
                String code = jsonObject.getString("code");
                String data = jsonObject.getString("data");
                if ("000000".equals(code)) {
                    return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(data);
                } else {
                    return new Result<String>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE.toString());
                }
            } else {
                return new Result<String>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.MOCK_APIERROR.getCode(), "调用查询Mock挡板配置信息接口报错!，url："+redisUrl), ex);
        }
        return new Result<String>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE.toString());
    }

}
