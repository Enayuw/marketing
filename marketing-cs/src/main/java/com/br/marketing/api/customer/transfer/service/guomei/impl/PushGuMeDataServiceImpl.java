package com.br.marketing.api.customer.transfer.service.guomei.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.api.customer.transfer.adapter.TransferDataAdaptee;
import com.br.marketing.api.customer.transfer.handler.CustomerHandlerEnum;
import com.br.marketing.api.customer.transfer.service.guomei.IGuoMeiDataService;
import com.br.marketing.api.customer.transfer.service.guomei.IPushGuMeDataService;
import com.br.marketing.api.customer.transfer.service.guomei.dto.GuMeResponseDTO;
import com.br.marketing.api.customer.transfer.service.guomei.dto.GuMeTransferJsonDTO;
import com.br.marketing.api.customer.transfer.service.guomei.dto.ResponseGuMeDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.entity.GuoMeiTransferData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 国美业务
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-16 17:06
 */
@Service
@Slf4j
public class PushGuMeDataServiceImpl implements IPushGuMeDataService {

    @Resource
    private IGuoMeiDataService guoMeiDataService;

    @Override
    public CustomerHandlerEnum customer() {
        return CustomerHandlerEnum.T_GUME;
    }

    @Override
    public TransferDataAdaptee parseObject(String jsonData) {
        return JSONObject.parseObject(jsonData, new TypeReference<GuMeTransferJsonDTO>() {
        }.getType());
    }

    @Override
    public CustomerResponseDTO verifyFields(TransferDataAdaptee adaptee) {
        GuMeTransferJsonDTO jsonDTO = (GuMeTransferJsonDTO) adaptee;
        GuMeResponseDTO responseGuMeDTO = new GuMeResponseDTO();
        boolean channelCodeBool;
        if (channelCodeBool = StringUtils.isNotBlank(jsonDTO.getChannelCode())) {
        } else {
            responseGuMeDTO.failed(",channelCode不可为空");
        }
        boolean requestIdBool;
        if (requestIdBool = StringUtils.isNotBlank(jsonDTO.getRequestId())) {
        } else {
            responseGuMeDTO.failed(",requestId不可为空");
        }
        boolean signBool;
        if (signBool = StringUtils.isNotBlank(jsonDTO.getSign())) {
        } else {
            responseGuMeDTO.failed(",sign不可为空");
        }
        if (channelCodeBool && requestIdBool && signBool) {
            // 验签
            String sign = Md5Utils.cell32(Md5Utils.cell32(jsonDTO.getRequestId() + jsonDTO.getChannelCode()
            ).toUpperCase(Locale.ROOT)).toUpperCase(Locale.ROOT);
            if (jsonDTO.getSign().equals(sign)) {
                // 验业务数据
                if (CollectionUtils.isEmpty(jsonDTO.getData())) {
                    responseGuMeDTO.failed(",data不可为空");
                } else {
                    return new CustomerResponseDTO(responseGuMeDTO.success()
                            , CustomerResponseDTO.StatusEnum.VALID, responseGuMeDTO.getCode());
                }
            } else {
                responseGuMeDTO.failed(",sign签名不正确");
            }
        }
        return new CustomerResponseDTO(responseGuMeDTO
                , CustomerResponseDTO.StatusEnum.INVALID, responseGuMeDTO.getCode());
    }

    @Override
    public int countBizDataNumber(TransferDataAdaptee adaptee) {
        GuMeTransferJsonDTO jsonDTO = (GuMeTransferJsonDTO) adaptee;
        return jsonDTO.getData() != null ? jsonDTO.getData().size() : 0;
    }

    @Override
    public Set<String> getBizAllFields(String jsonStr) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Set<String> set = jsonObject.keySet();
        HashSet<String> fieldSet = new HashSet<>(set);
        String arrayKey = "data";
        if (jsonObject.containsKey(arrayKey)) {
            JSONArray data = jsonObject.getJSONArray(arrayKey);
            int size = data.size();
            for (int i = 0; i < size; i++) {
                fieldSet.addAll(data.getJSONObject(i).keySet());
            }
        }
        return fieldSet;
    }

    @Override
    public CustomerResponseDTO jsonErrorResponse(Exception e) {
        GuMeResponseDTO responseGuMeDTO = new GuMeResponseDTO();
        responseGuMeDTO.failed(",json解析失败");
        return new CustomerResponseDTO(responseGuMeDTO
                , CustomerResponseDTO.StatusEnum.INVALID, responseGuMeDTO.getCode());
    }

    @Override
    public CustomerResponseDTO bizErrorResponse(Exception e) {
        return fallbackResponse(e);
    }

    @Override
    public CustomerResponseDTO fallbackResponse(Exception e) {
        GuMeResponseDTO responseGuMeDTO = new GuMeResponseDTO();
        responseGuMeDTO.failed();
        return new CustomerResponseDTO(responseGuMeDTO
                , CustomerResponseDTO.StatusEnum.INVALID, responseGuMeDTO.getCode());
    }


    @Override
    public ResponseCustomDTO saveTransferData(String apiCode, String jsonData) {
        ResponseGuMeDTO responseGuMeDTO = new ResponseGuMeDTO();
        GuMeTransferJsonDTO jsonDTO;
        GuoMeiTransferData guoMeiTransferData = new GuoMeiTransferData();
        guoMeiTransferData.setCreateDate(LocalDate.now().toString());
        guoMeiTransferData.setCreateTime(new Date());
        guoMeiTransferData.setUpdateTime(guoMeiTransferData.getCreateTime());
        guoMeiTransferData.setJsonData(jsonData);
        guoMeiTransferData.setApiCode(apiCode);
        try {
            jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<GuMeTransferJsonDTO>() {
            }.getType());
            if (transferApiParamRightfulCheck(jsonDTO, responseGuMeDTO, guoMeiTransferData)) {
                responseGuMeDTO.success();
                guoMeiTransferData.setStatus(1);
            } else {
                guoMeiTransferData.setStatus(0);
                guoMeiTransferData.setErrorMsg(responseGuMeDTO.getMessage());
            }
        } catch (Exception e) {
            responseGuMeDTO.failed("json解析失败");
            guoMeiTransferData.setErrorMsg(responseGuMeDTO.getMessage().concat(":") + e.getMessage());
            log.error(e.getMessage(), e);
        }
        try {
            guoMeiDataService.saveTransferDataHandler(guoMeiTransferData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                sendQueue(guoMeiTransferData);
            } catch (PulsarClientException clientException) {
                responseGuMeDTO.failed();
                log.error(clientException.getMessage(), clientException);
            }
        }
        return responseGuMeDTO;
    }

    /**
     * 2023-10-16 18:08
     * 转化接口参数合法检查
     */
    @Deprecated
    private boolean transferApiParamRightfulCheck(GuMeTransferJsonDTO jsonDTO
            , ResponseGuMeDTO responseGuMeDTO, GuoMeiTransferData guoMeiTransferData) {
        boolean channelCodeBool;
        if (channelCodeBool = StringUtils.isNotBlank(jsonDTO.getChannelCode())) {
            guoMeiTransferData.setChannelcode(jsonDTO.getChannelCode());
        } else {
            responseGuMeDTO.failed(",channelCode不可为空");
        }
        boolean requestIdBool;
        if (requestIdBool = StringUtils.isNotBlank(jsonDTO.getRequestId())) {
            guoMeiTransferData.setRequestid(jsonDTO.getRequestId());
        } else {
            responseGuMeDTO.failed(",requestId不可为空");
        }
        boolean signBool;
        if (signBool = StringUtils.isNotBlank(jsonDTO.getSign())) {
            guoMeiTransferData.setSign(jsonDTO.getSign());
        } else {
            responseGuMeDTO.failed(",sign不可为空");
        }
        if (channelCodeBool && requestIdBool && signBool) {
            // 验签
            boolean sign2Bool;
            String sign = Md5Utils.cell32(Md5Utils.cell32(jsonDTO.getRequestId() + jsonDTO.getChannelCode()
            ).toUpperCase(Locale.ROOT)).toUpperCase(Locale.ROOT);
            if (sign2Bool = !jsonDTO.getSign().equals(sign)) {
                responseGuMeDTO.failed(",sign签名不正确");
            }
            // 验业务数据
            boolean dataBool;
            if (dataBool = CollectionUtils.isEmpty(jsonDTO.getData())) {
                responseGuMeDTO.failed(",data不可为空");
            }
            return !sign2Bool && !dataBool;
        }
        return false;
    }


    @Override
    public Result<Boolean> consumerTransfer(String msg) {
        Result<Boolean> result = new Result<>();
        GuoMeiTransferData guoMeiTransferData = JSONObject.parseObject(msg, new TypeReference<GuoMeiTransferData>() {
        }.getType());
        try {
            int b = guoMeiDataService.saveTransferDataHandler(guoMeiTransferData);
            result.setCode(b > 0 ? ResultCode.SUCCESS.getValue() : ResultCode.FAIL.getValue());
        } catch (Exception e) {
            log.error(e.getMessage());
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }
}
