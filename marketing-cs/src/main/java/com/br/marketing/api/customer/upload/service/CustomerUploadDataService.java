package com.br.marketing.api.customer.upload.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.ResponseCustomDTO;

/**
 * 定制客户上传数据处理
 *
 * @author senyang.zheng
 * @date 2024/08/05
 */
public interface CustomerUploadDataService {

    /**
     * 接收客户定制上传数据
     *
     * @param apiCode  api代码
     * @param jsonData json数据
     * @return {@link ResponseCustomDTO }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    ResponseCustomDTO receiveCustomizeUploadData(String apiCode, String jsonData);


    /**
     * 异常消息重新入库
     *
     * @param msg 补偿数据
     * @return {@link Result }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/08/07
     */
    Result<Boolean> consumerUploadPayData(String msg);
}
