package com.br.marketing.api.customer.transfer.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.ResponseCustomDTO;

/**
 * 定制客户转化数据处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-18 16:04
 */
public interface CustomerTransferDataService {


    /**
     * 接入（客户订制）转化数据
     *
     * @param apiCode  apiCode
     * @param jsonData 业务数据
     * @return ResponseGuMeDTO
     * @author Guo Zeqiang
     */
    ResponseCustomDTO receiveTransferDataHandler(String apiCode, String jsonData);

    /**
     * 2023-10-17 17:56
     * 异常消息重新入库
     * 补偿数据
     *
     * @param msg mq中的消息
     * @return 结果
     */
    Result<Boolean> consumerTransferPayData(String msg);
}
