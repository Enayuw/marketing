package com.br.marketing.client.bi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.bi.input.OffLineScoreDTO;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
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


@Service
@Slf4j
public class BiApiClient {

    @Autowired
    RestTemplate restTemplate;

    @Value(value = "${api.bi.executeTask:00}")
    private String executeTaskUrl;

    @Value(value = "${api.bi.token:00}")
    private String token;

    @Qualifier("logDbpool")
    @Autowired
    public ThreadPoolExecutor logDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;


    public Result reqOffLineJob(OffLineScoreDTO dto) {
        dto.setToken(token);
        ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate, interfaceLogMapper, logDbpool)
                .setUrl(executeTaskUrl).setRequestParam(dto)
                .setContentType(MediaType.APPLICATION_JSON_UTF8).postTransferStr();
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
