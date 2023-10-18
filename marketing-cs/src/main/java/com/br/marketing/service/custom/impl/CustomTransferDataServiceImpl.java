package com.br.marketing.service.custom.impl;

import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.service.custom.CustomTransferDataService;
import com.br.marketing.service.custom.handler.CustomDataHandleSingleton;
import com.br.marketing.service.custom.handler.CustomDataHandler;
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
public class CustomTransferDataServiceImpl implements CustomTransferDataService {

    @Resource
    private CustomDataHandleSingleton customDataHandleFactory;

    @Override
    public ResponseCustomDTO receiveTransferDataHandler(String apiCode, String jsonData) {
        CustomDataHandler customDataHandleImpl = customDataHandleFactory.getCustomDataHandleImpl(apiCode);
        return customDataHandleImpl.receiveCustomDataHandler(apiCode, jsonData);
    }
}
