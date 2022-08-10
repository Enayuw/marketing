package com.br.marketing.client.bi;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.bi.input.OffLineScoreDTO;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class BiApiClient {

    @Autowired
    RestTemplate restTemplate;

    @Value(value = "${api.bi.executeTask:00}")
    private String executeTaskUrl;

    public Result reqOffLineJob(OffLineScoreDTO dto){
        ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate)
                .setUrl(executeTaskUrl).setRequestParam(dto)
                .setContentType(MediaType.APPLICATION_JSON_UTF8).postTransferStr();
        if(200==transfer.getHttpCode()){
            //todo 根据接口code 判断是否成功
            log.warn(JSON.toJSONString(transfer));
            return null;
        }
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(transfer.getResult());
    }
}
