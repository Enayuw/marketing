package com.br.marketing.service;

import com.br.marketing.dto.ResponseCustomDTO;

/**
 * 国美推转化数据接口
 *
 * @author Guo Zeqiang
 * @dateTime 2023/10/16 16:24
 */
public interface IPushGuMeDataService {


    /**
     * 保存国美（客户订制）转化数据
     *
     * @param apiCode  apiCode
     * @param jsonData 业务数据
     * @return ResponseGuMeDTO
     * @author Guo Zeqiang
     */
    ResponseCustomDTO saveTransferData(String apiCode, String jsonData);


}
