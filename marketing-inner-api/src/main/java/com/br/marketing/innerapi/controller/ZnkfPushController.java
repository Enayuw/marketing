package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSON;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.customer.CallRecordDTO;
import com.br.marketing.dto.customer.SmsRecordDTO;
import com.br.marketing.dto.derived.CustDerivedItemVO;
import com.br.marketing.dto.derived.CustDerivedQueryRequest;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.service.derived.CustDerivedQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 智能客服推送接口
 */
@RestController
@RequestMapping("/znkePush")
@Tag(name = "客服推送营销数据", description = "客服推送营销数据")
public class ZnkfPushController {

    private static final Logger log = LoggerFactory.getLogger(ZnkfPushController.class);

    @Autowired
    private ZnkfPushService znkfPushService;

    @Resource
    private DassServiceClient dassServiceClient;

    @Resource
    private CustDerivedQueryService custDerivedQueryService;

    @Operation(summary = "客服推送营销数据 回调接口")
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

    @Operation(summary = "短信回调接口")
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

    @Operation(summary = "短信发送即回调接口")
    @PostMapping("/smsCallBackAtOnce")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public String smsCallBackAtOnce(@RequestBody SmsRecordDTO dto) {
        try {
            return znkfPushService.smsCallBackAtOnce(dto);
        }catch (Exception ex){
            log.error(ex.getMessage());
            return "fail";
        }
    }

    @Operation(summary = "客服推送营销黑名单结束标识接口")
    @PostMapping("/znkfPushBlackPhoneMark")
    public ApiResult znkfPushBlackPhoneMark(String apiCode, String pushDate) {
        try {
            return znkfPushService.znkfPushBlackPhoneMark(apiCode,pushDate);
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Operation(summary = "测试电销转化接口")
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
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), ex.getMessage()), ex);
            throw ex;
        }
    }

    @Operation(summary = "360查询券等衍生信息接口")
    @PostMapping("/custDerivedQuery")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<List<CustDerivedItemVO>> custDerivedQuery(@RequestBody @Valid CustDerivedQueryRequest request) {
        return custDerivedQueryService.queryByCustNumList(request);
    }

    @Operation(summary = "接收回调数据并入库（通用接口，支持不同版本）")
    @PostMapping("/callbackDataInsert")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public String callbackDataInsert(@RequestBody String jsonData) {
        try {
            return znkfPushService.callbackDataInsert(jsonData);
        } catch (Exception ex) {
            log.error("接收回调数据并入库失败，错误信息：{}", ex.getMessage(), ex);
            return "fail";
        }
    }
}
