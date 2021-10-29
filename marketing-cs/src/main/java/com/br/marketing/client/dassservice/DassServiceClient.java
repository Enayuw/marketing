package com.br.marketing.client.dassservice;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@Service
@Slf4j
public class DassServiceClient {



    @Value("${api.dass.SecretKey:00}")
    private String secretKey;

    @Value("${api.dass.postHermesUserData:00}")
    private String postHermesUserDataUrl;
    @Value("${api.dass.isProxy:0}")
    private String isProxy;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    @Qualifier("restTemplateByProxy")
    RestTemplate restTemplateByProxy;

    public Result postHermesUserData(List<DassImportDataDTO> dtos,Integer retryIndex){
        long l = LocalDateTime.now().plusMinutes(10L).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        String sign = DigestUtils.md5DigestAsHex(String.format(secretKey + "data=%s&ts=%d", JSON.toJSONString(dtos), l).getBytes());
        HashMap requestParam = new HashMap();
        requestParam.put("ts",l);
        requestParam.put("sign",sign);
        requestParam.put("data", JSON.toJSONString(dtos));

        Boolean mark = false;
        ThirdApiResultTransfer transfer = new ApiCaller(isProxy.equals("0") ? restTemplate : restTemplateByProxy)
                .setRequestParam(requestParam)
                .setUrl(postHermesUserDataUrl)
                .setContentType(MediaType.APPLICATION_JSON_UTF8)
                .postTransferStr();
        if(transfer.getHttpCode() == 200){
            mark = true;
            String result = transfer.getResult();
        }else{
            if(retryIndex == 4) {
                log.error(String.format("调用接口重试报错 url:%s;code:%d,message:%s"
                        ,postHermesUserDataUrl,transfer.getHttpCode(),transfer.getResult()));
            }else {
                retryIndex++;
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.postHermesUserData(dtos, retryIndex);
            }
        }
        if(mark){
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }else{
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
    }
}
