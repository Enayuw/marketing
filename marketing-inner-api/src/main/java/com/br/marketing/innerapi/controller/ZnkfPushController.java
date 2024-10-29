package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSON;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.customer.CallRecordDTO;
import com.br.marketing.dto.customer.SmsRecordDTO;
import com.br.marketing.service.ZnkfPushService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 智能客服推送接口
 */
@RestController
@RequestMapping("/znkePush")
@Api(value = "客服推送营销数据")
public class ZnkfPushController {

    private static final Logger log = LoggerFactory.getLogger(ZnkfPushController.class);

    @Autowired
    private ZnkfPushService znkfPushService;

    @Resource
    private DassServiceClient dassServiceClient;

    @ApiOperation(value = "客服推送营销数据 回调接口")
    @PostMapping("/znkfPushCallBack")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public String znkfPushCallBack(@RequestBody CallRecordDTO dto) {
        try {
            return znkfPushService.znkfPushCallBack(dto);
        }catch (Exception ex){
            log.error(ex.getMessage());
            return "fail";
        }
    }

    @ApiOperation(value = "短信回调接口")
    @PostMapping("/smsCallBack")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public String smsCallBack(@RequestBody SmsRecordDTO dto) {
        try {
            return znkfPushService.smsCallBack(dto);
        }catch (Exception ex){
            log.error(ex.getMessage());
            return "fail";
        }
    }

    @ApiOperation(value = "客服推送营销黑名单结束标识接口")
    @PostMapping("/znkfPushBlackPhoneMark")
    public ApiResult znkfPushBlackPhoneMark(String apiCode, String pushDate) {
        try {
            return znkfPushService.znkfPushBlackPhoneMark(apiCode,pushDate);
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @ApiOperation(value = "测试电销转化接口")
    @PostMapping("/testDassTransferData")
    public ApiResult testDassTransferData() {
        try {
            DassTransferDataAdapDTO dassTransferDataAdapDTO = new DassTransferDataAdapDTO();
            List<DassTransferDataDTO> dassTransferDataDTOList = new ArrayList<>();
            DassTransferDataDTO dassTransferDataDTO = new DassTransferDataDTO();
            dassTransferDataDTO.setIfTransform("1");
            dassTransferDataDTO.setUid("2312432");
            dassTransferDataDTO.setOrgName("ppd");
            dassTransferDataDTOList.add(dassTransferDataDTO);
            dassTransferDataAdapDTO.setDassTransferDataDTOList(dassTransferDataDTOList);
            dassTransferDataAdapDTO.setTransferInfoId(234L);
            Result result = dassServiceClient.postTransferData(dassTransferDataAdapDTO);
            log.warn("调用电销返回result={}", JSON.toJSONString(result));
            return new ApiResult().success();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw ex;
        }
    }
}
