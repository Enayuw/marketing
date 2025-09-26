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

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName MarketingMockApiService
 * @Description TODO
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
     * Mock挡板查询redis缓存
     *
     * @param cacheKey redis key
     * @return Result<Boolean>
     */
    @RetryMethod(retryNowNum = 2)
    public Result<String> getMockRedisValue(String cacheKey) {
        try {
            ThirdApiResultTransfer result = new ApiCallerUtil(restTemplate, interfaceLogMapper, interfaceLogDbpool)
                    .setUrl(redisUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(cacheKey)
                    .setEncode(Boolean.TRUE)
                    .postTransferStr();
            if (Integer.valueOf(200).equals(result.getHttpCode())) {
                JSONObject jsonObject = JSON.parseObject(result.getResult());
                String code = jsonObject.getString("code");
                String data = jsonObject.getString("data");
                if ("00".equals(code)) {
                    return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(data);
                } else {
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
                }
            } else {
                return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_APIERROR.getCode(), "调用Mock挡板查询redis缓存接口报错!"), ex);
        }
        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
    }

}
