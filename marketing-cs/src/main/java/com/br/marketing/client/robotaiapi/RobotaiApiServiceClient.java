package com.br.marketing.client.robotaiapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RobotaiApiServiceClient {

    @Value("${api.robotAiApiService.robotOutboundUrl:00}")
    private String robotOutboundUrl;

    @Autowired
    RestTemplate restTemplate;

    public TransferRobotOutboundVO<UnsuccessfulData> pushRobotai(TransferRobotOutboundDTO dto){
        try{
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate).setUrl(robotOutboundUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
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
