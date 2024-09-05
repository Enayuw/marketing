package com.br.marketing.api.customer.transfer.service.guomei;

import com.br.marketing.api.customer.transfer.handler.CustomerDataHandler;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.ResponseCustomDTO;

/**
 * 国美推送转化数据定制接口
 *
 * @author Guo Zeqiang
 * @dateTime 2023/10/16 16:24
 */
public interface IPushGuMeDataService extends CustomerDataHandler {


    /**
     * 接入国美（客户订制）转化数据
     *
     * @param apiCode  apiCode
     * @param jsonData 业务数据
     * @return ResponseGuMeDTO
     * @author Guo Zeqiang
     */
    @Deprecated
    ResponseCustomDTO saveTransferData(String apiCode, String jsonData);


    /**
     * 2023-10-17 17:56
     * 异常消息重新入库
     *
     * @param msg mq中的消息
     * @return 结果
     */
    @Deprecated
    Result<Boolean> consumerTransfer(String msg);
}
