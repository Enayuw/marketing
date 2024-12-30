package com.br.marketing.service.clean.guomei;

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

    /**
     * 国美前置黑名单数据清洗
     *
     * @param message 信息
     * @return {@link Result }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/10/30
     */
    Result<Boolean> cleanBlackData(String message);
}
