package com.br.marketing.client.baiying;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.baiying.input.BlacklistDataDTO;
import com.br.marketing.client.baiying.input.ReqBlacklistDTO;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.mapper.InterfaceLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName ByApiServiceClient
 * @Description 百应黑名单接口
 * @Author kongbx
 * @Date 2024/5/27 11:31
 */
@Service
@Slf4j
public class ByApiServiceClient {

    @Autowired
    RestTemplate restTemplate;

    @Value(value = "${api.baiying.baseUrl:00}")
    private String pushBlackDataUrl;

    @Qualifier("logDbpool")
    @Autowired
    public ThreadPoolExecutor logDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;

    @RetryMethod(retryNowNum = 3)
    public Result pushBaiying(ReqBlacklistDTO dto){
        log.info("pushBaiying request:{}", JSONObject.toJSONString(dto));
        ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate, interfaceLogMapper, logDbpool)
                .setUrl(pushBlackDataUrl)
                .setRequestParam(dto)
                .setContentType(MediaType.APPLICATION_JSON_UTF8).postTransferStr();
        log.info("pushBaiying result:{}", JSONObject.toJSONString(transfer));
        if (200 == transfer.getHttpCode()) {
            JSONObject jsonObject = JSON.parseObject(transfer.getResult());
            if (jsonObject == null) {
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(transfer.getResult());
            }
            if ("000000".equals(jsonObject.getString("code"))) {
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            } else {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(transfer.getResult());
            }
        }
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(transfer.getResult());
    }
}
