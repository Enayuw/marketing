package com.br.marketing.service.clean.hengchang;

import com.br.marketing.common.commondto.Result;

/**
 * @ClassName HengChangDataCleanService
 * @Description 恒昌数据接入
 * @Author kongbx
 * @Date 2025/1/4 10:39
 */
public interface HengChangDataCleanService {

    /**
     * 恒昌数据接入后置清洗
     *
     * @param message 消息体
     * @return {@link Result }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/10/23
     */
    Result<Boolean> cleanData(String message);
}
