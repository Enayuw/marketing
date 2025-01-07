package com.br.marketing.api.customer.transfer.service.hengchang.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.transfer.adapter.TransferDataAdaptee;
import com.br.marketing.api.customer.transfer.handler.CustomerHandlerEnum;
import com.br.marketing.api.customer.transfer.service.hengchang.IPushHengChangDataService;
import com.br.marketing.api.customer.transfer.service.hengchang.dto.HengChangResponseDTO;
import com.br.marketing.api.customer.transfer.service.hengchang.dto.HengChangTransferJsonDTO;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @ClassName IPushHengChangDataServiceImpl
 * @Description 恒昌转化
 * @Author kongbx
 * @Date 2025/1/7 13:46
 */
@Service
@Slf4j
public class IPushHengChangDataServiceImpl implements IPushHengChangDataService {
    @Resource
    private MarketingCustomerMapper marketingCustomerService;
    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public CustomerHandlerEnum customer() {
        return CustomerHandlerEnum.T_HENGCHANG;
    }

    @Override
    public TransferDataAdaptee parseObject(String jsonData) {
        HengChangTransferJsonDTO object = JSONObject.parseObject(jsonData, new TypeReference<HengChangTransferJsonDTO>() {
        }.getType());
        object.setMarketingSyncInfoMapper(marketingSyncUserMapper);
        return object;
    }

    @Override
    public CustomerResponseDTO verifyFields(TransferDataAdaptee adaptee) {
        HengChangResponseDTO HengChangResponseDTO = new HengChangResponseDTO();
        HengChangResponseDTO.success();
        return new CustomerResponseDTO(HengChangResponseDTO
                , CustomerResponseDTO.StatusEnum.VALID, HengChangResponseDTO.getCode());
    }

    @Override
    public int countBizDataNumber(TransferDataAdaptee adaptee) {
        return countBizDataNumber(adaptee.getJsonData());
    }

    @Override
    public Set<String> getBizAllFields(String jsonStr) {
        return null;
    }

    @Override
    public CustomerResponseDTO jsonErrorResponse(Exception e) {
        HengChangResponseDTO HengChangResponseDTO = new HengChangResponseDTO();
        HengChangResponseDTO.failed(MarketingErrorInfo.JSON_DATA_ERROR);
        return new CustomerResponseDTO(HengChangResponseDTO
                , CustomerResponseDTO.StatusEnum.INVALID, HengChangResponseDTO.getCode());
    }

    @Override
    public CustomerResponseDTO bizErrorResponse(Exception e) {
        return fallbackResponse(e);
    }

    @Override
    public CustomerResponseDTO fallbackResponse(Exception e) {
        HengChangResponseDTO HengChangResponseDTO = new HengChangResponseDTO();
        HengChangResponseDTO.failed(MarketingErrorInfo.UNKNOWN_ERROR);
        return new CustomerResponseDTO(HengChangResponseDTO
                , CustomerResponseDTO.StatusEnum.INVALID, HengChangResponseDTO.getCode());
    }
}
