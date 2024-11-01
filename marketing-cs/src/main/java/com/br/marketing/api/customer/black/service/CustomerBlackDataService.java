package com.br.marketing.api.customer.black.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.ResponseCustomDTO;

/**
 * 定制客户上传数据处理
 *
 * @author senyang.zheng
 * @date 2024/08/05
 */
public interface CustomerBlackDataService {


    /**
     * 接收客户定制黑名单数据
     *
     * @param apiCode  API代码
     * @param jsonData json数据
     * @return {@link ResponseCustomDTO }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    ResponseCustomDTO receiveCustomizeBlackData(String apiCode, String jsonData);


    /**
     * 异常消息重新入库
     *
     * @param msg 味精
     * @return {@link Result }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/10/30
     */
    Result<Boolean> consumerBlackPayData(String msg);
}
