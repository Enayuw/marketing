package com.br.marketing.client.twosevenservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.twosevenservice.intput.RequestSevenDTO;
import com.br.marketing.client.twosevenservice.output.ResponseSevenZDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
public class TwoSevenService {

    @Value("${api.twoSeven.zhuangku:00}")
    private String url;

    @Value("${api.twoSeven.isProxy:0}")
    private String isProxy;

    @Autowired
    HttpProxyClient httpProxyClient;

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<ResponseSevenZDTO> requestTransferStatus(RequestSevenDTO dto,String extendInfo){
        try {
            dto.setUserName("1078");
            dto.setPwd("2021&ert");
            HashMap<String, String> response = httpProxyClient.sendByCode(dto
                    , url
                    , isProxy.equals("1") ? true : false
                    , MediaType.APPLICATION_FORM_URLENCODED_VALUE
                    , extendInfo);
            String code = response.get("httpcode");
            if("200".equals(code)){
                ResponseSevenZDTO content = JSON.parseObject(response.get("content"), new TypeReference<ResponseSevenZDTO>() {
                }.getType());
                if(!"200".equals(content.getRet())){
                    log.error(String.format("调用七七撞库返回状态码异常：%s",response.get("content")));
                }
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(content);
            }else{
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }
        }catch (Exception ex){
            log.error(String.format("调用七七撞库接口异常：%s",ex.getMessage()),ex);
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
    }
}
