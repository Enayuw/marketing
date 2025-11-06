package com.br.marketing.client.ibmpapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.ibmpapi.outpu.TransferIbmpOutboundVO;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.client.robotaiapi.output.TransferRobotDataVO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
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

@Service
@Slf4j
public class IbmpApiServiceClient {

    @Value("${api.imbpApiService.lineBaseInfoUrl:00}")
    private String lineBaseInfoUrl;


    @Autowired
    RestTemplate restTemplate;

    @Qualifier("logDbpool")
    @Autowired
    public ThreadPoolExecutor logDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;


    public TransferIbmpOutboundVO getLineBaseInfo() {
        log.warn("getLineBaseInfo lineBaseInfoUrl:{}",lineBaseInfoUrl);
        try {
            ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate,interfaceLogMapper,logDbpool).setUrl(lineBaseInfoUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .postTransferStr();
            if(!Integer.valueOf(200).equals(transfer.getHttpCode())){
                throw new RuntimeException("获取IBMP线路基础信息异常：".concat(String.valueOf(transfer.getHttpCode())));
            }
            return JSON.parseObject(transfer.getResult()
                    ,new TypeReference<TransferIbmpOutboundVO>(){}.getType());
        }catch (Exception ex){
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.IBMP_LINE_SERVICEERROR.getCode(), ex.getMessage()), ex);
            TransferIbmpOutboundVO result = new TransferIbmpOutboundVO();
            result.setCode("9999");
            result.setMessage(ex.getMessage());
            return result;
        }
    }

}
