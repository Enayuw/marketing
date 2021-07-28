package com.br.marketing.client.intelligentcustomerservice;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.ParamsValidAspect;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.entity.CustomerInfoPushLog;
import com.br.marketing.mapper.CustomerInfoPushLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IntelligentCustomerServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentCustomerServiceClient.class);

    @Value("${api.intelligentCustomerService.pushUserUrl:00}")
    private String pushUrl;

    @Autowired
    CustomerInfoPushLogMapper customerInfoPushLogMapper;

    @Autowired
    RestTemplate restTemplate;


    public Result<Integer> pushUser(PushMarketingUserDTO dto,Long mId,String pushBatch){
//        System.out.println("SERVICE===="+apiCaller);
//        System.out.println("SERVICE===="+JSON.toJSONString(dto));
        Result result = new Result();
        CustomerInfoPushLog log = new CustomerInfoPushLog();
        log.setmId(mId);
        log.setBatch(pushBatch);
        String s = JSON.toJSONString(dto);
        log.setParam(s.length()>4999?s.substring(0,4999):s);
//        log.setParam("");
        try{
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate).setUrl(pushUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            log.setResultContent(transfer.getResult().length()>4999?transfer.getResult().substring(0,4999):transfer.getResult());
            log.setHttpStatus(String.valueOf(transfer.getHttpCode()));
            JSONObject jsonObject = JSON.parseObject(transfer.getResult());
            log.setCode(jsonObject.getString("code"));
            if("000000".equals(jsonObject.getString("code"))){
                result.setCode(ResultCode.SUCCESS.getValue());
            }else{
                result.setCode(ResultCode.FAIL.getValue()).setMessage(jsonObject.getString("message"));
            }
        }catch (Exception ex){
            log.setErrorContent(ex.getMessage());
            result.setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
        log.setCreateTime(new Date());
        customerInfoPushLogMapper.insertSelective(log);
        return result;
    }


    public Result<String> getUserStatus(PushMarketingUserDTO dto){
        Result<String> result = new Result();
        try{
            ThirdApiResultTransfer transfer = new ApiCaller().setUrl(pushUrl)
                    .setContentType(MediaType.MULTIPART_FORM_DATA)
                    .setRequestParam(dto).postTransferStr();
            JSONObject jsonObject = JSON.parseObject(transfer.getResult());
            result.setCode(ResultCode.SUCCESS.getValue()).setDate(jsonObject.getString("code"));
        }catch (Exception ex){
            result.setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
        return result;
    }
}
