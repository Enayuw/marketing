package com.br.marketing.client.robotaiapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.common.utils.net.InterfaceLog;
import com.br.marketing.common.utils.net.MomCommonUtil;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ThreadPoolExecutor;

@Service
public class RobotaiApiServiceClient {

    @Value("${api.robotAiApiService.robotOutboundUrl:00}")
    private String robotOutboundUrl;

    @Value("${api.customerService.apiCode:0}")
    private String customerServiceApiCode;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MomCommonUtil momCommonUtil;

    @Qualifier("logDbpool")
    @Autowired
    public ThreadPoolExecutor logDbpool;

    public TransferRobotOutboundVO<UnsuccessfulData> pushRobotai(TransferRobotOutboundDTO dto,String requestId){
        dto.getJsonData().setPlatApiCode(customerServiceApiCode);
        try{
            InterfaceLog interfaceLog = new InterfaceLog();
            interfaceLog.setApiCode(dto.getApiCode());
            interfaceLog.setSwiftNumber(requestId);
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate,momCommonUtil,logDbpool).setUrl(robotOutboundUrl)
                    .setInterfaceLog(interfaceLog)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            if(!Integer.valueOf(200).equals(transfer.getHttpCode())){
                throw new RuntimeException("客服中心：".concat(String.valueOf(transfer.getHttpCode())));
            }
            TransferRobotOutboundVO<UnsuccessfulData> result = JSON.parseObject(transfer.getResult()
                    ,new TypeReference<TransferRobotOutboundVO>(){}.getType());
            return result;
        }catch (Exception ex){
            TransferRobotOutboundVO<UnsuccessfulData> result = new TransferRobotOutboundVO();
            result.setCode("9999");
            result.setMessage(ex.getMessage());
            return result;
        }
    }
}
