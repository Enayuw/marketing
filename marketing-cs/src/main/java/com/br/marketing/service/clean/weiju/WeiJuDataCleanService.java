package com.br.marketing.service.clean.weiju;

import com.br.marketing.common.commondto.Result;

public interface WeiJuDataCleanService {

    /**
     * 微聚数据接入后置清洗
     *
     * @param message 消息体
     * @return {@link Result }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/10/23
     */
    Result<Boolean> cleanData(String message);
}
