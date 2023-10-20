package com.br.marketing.service.customer.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.service.customer.CustomerTransferDataService;
import com.br.marketing.service.customer.handler.CustomerDataHandleSingleton;
import com.br.marketing.service.customer.handler.CustomerDataHandler;
import com.br.marketing.service.customer.handler.CustomerHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 定制客户转化数据处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-18 16:05
 */
@Service
@Slf4j
public class CustomerTransferDataServiceImpl implements CustomerTransferDataService {

    @Resource
    private CustomerDataHandleSingleton customDataHandleFactory;

    @Override
    public ResponseCustomDTO receiveTransferDataHandler(String apiCode, String jsonData) {
        CustomerDataHandler customDataHandleImpl = customDataHandleFactory.getCustomDataHandleImpl(apiCode
                , CustomerHandlerEnum.T_ALIEN_DEFAULT);
        return customDataHandleImpl.receiveCustomDataHandler(apiCode, jsonData);
    }

    @Override
    public Result<Boolean> consumerTransferPayData(String msg) {
        JSONObject jsonObject = JSONObject.parseObject(msg);
        CustomerHandlerEnum anEnum = jsonObject.getObject("enum", CustomerHandlerEnum.class);
        CustomerDataHandler customDataHandleImpl = customDataHandleFactory.getCustomDataHandleImpl(anEnum);
        return customDataHandleImpl.consumerPayData(jsonObject.getString("jsonData"));
    }
}
