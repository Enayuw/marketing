package com.br.marketing.client.intelligentcustomerservice;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.entity.CustomerInfoPushLog;
import com.br.marketing.mapper.CustomerInfoPushLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IntelligentCustomerServiceClient {



    @Value("${api.intelligentCustomerService.pushUserUrl}")
    private String pushUrl;

    @Autowired
    CustomerInfoPushLogMapper customerInfoPushLogMapper;

    @Autowired
    RestTemplate restTemplate;


    public Result pushUser(PushMarketingUserDTO dto,Long mId,String pushBatch){
//        System.out.println("SERVICE===="+apiCaller);
//        System.out.println("SERVICE===="+JSON.toJSONString(dto));
        Result result = new Result();
        CustomerInfoPushLog log = new CustomerInfoPushLog();
        log.setmId(mId);
        log.setBatch(pushBatch);
        log.setParam(JSON.toJSONString(dto));
        try{
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate).setUrl(pushUrl)
                    .setContentType(MediaType.MULTIPART_FORM_DATA)
                    .setRequestParam(dto).postTransferStr();
            log.setResultContent(transfer.getResult());
            log.setHttpStatus(String.valueOf(transfer.getHttpCode()));
            JSONObject jsonObject = JSON.parseObject(transfer.getResult());
            log.setCode(jsonObject.getString("code"));
            if("000000".equals(jsonObject.getString("code"))){
                result.setCode(ResultCode.SUCCESS.getValue());
                log.setRealStauts(2);
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


    public Result getUserStatus(PushMarketingUserDTO dto){
        Result result = new Result();
        try{
            ThirdApiResultTransfer transfer = new ApiCaller().setUrl(pushUrl)
                    .setContentType(MediaType.MULTIPART_FORM_DATA)
                    .setRequestParam(dto).postTransferStr();
            JSONObject jsonObject = JSON.parseObject(transfer.getResult());
            if("000000".equals(jsonObject.getString("code"))){
                result.setCode(ResultCode.SUCCESS.getValue()).setDate(jsonObject.getString("result"));
            }else{
                result.setCode(ResultCode.FAIL.getValue()).setMessage(jsonObject.getString("message"));
            }
        }catch (Exception ex){
            result.setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
        return result;
    }
}
