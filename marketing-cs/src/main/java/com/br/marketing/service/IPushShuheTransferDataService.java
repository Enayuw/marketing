package com.br.marketing.service;

import com.br.marketing.dto.ResponseCustomDTO;

/**
 * 数禾推转化数据接口
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 14:24
 */
public interface IPushShuheTransferDataService {


    /**
     * 插入数禾（客户订制）转化数据
     *
     * @param apiCode  apiCode
     * @param jsonData 业务数据
     * @return ResponseShuheDTO
     * @author Guo Zeqiang
     */
    ResponseCustomDTO insertShuheTransferData(String apiCode, String jsonData);
}
