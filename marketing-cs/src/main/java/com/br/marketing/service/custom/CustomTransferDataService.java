package com.br.marketing.service.custom;

import com.br.marketing.dto.ResponseCustomDTO;

/**
 * 定制客户转化数据处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-18 16:04
 */
public interface CustomTransferDataService {


    /**
     * 接入（客户订制）转化数据
     *
     * @param apiCode  apiCode
     * @param jsonData 业务数据
     * @return ResponseGuMeDTO
     * @author Guo Zeqiang
     */
    ResponseCustomDTO receiveTransferDataHandler(String apiCode, String jsonData);
}
