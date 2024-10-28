package com.br.marketing.service.guomei;

import com.br.marketing.common.commondto.Result;

public interface GuoMeiDataCleanService {
    /**
     * 国美前置数据清洗接口
     *
     * @param message 信息
     * @return {@link Result }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/10/28
     */
    Result<Boolean> cleanData(String message);
}
