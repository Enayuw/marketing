package com.br.marketing.service.customer.alien;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.service.customer.handler.CustomerDataHandler;
import com.br.marketing.service.customer.handler.CustomerHandlerEnum;

/**
 * 转化数据陌生客户处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-20 14:53
 */
public class AlienTransferDataServiceImpl implements CustomerDataHandler {
    @Override
    public CustomerHandlerEnum custom() {
        return CustomerHandlerEnum.T_ALIEN_DEFAULT;
    }

    @Override
    public ResponseCustomDTO receiveCustomDataHandler(String apiCode, String jsonData) {
        return null;
    }

    @Override
    public Result<Boolean> consumerPayData(String msg) {
        return null;
    }
}
