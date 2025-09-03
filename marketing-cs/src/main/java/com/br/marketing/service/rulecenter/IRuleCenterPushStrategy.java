package com.br.marketing.service.rulecenter;

import com.br.marketing.common.commondto.Result;

/**
 * 推送策略接口
 */
public interface IRuleCenterPushStrategy {


    /**
     * 执行推送逻辑
     *
     * @param context 推送上下文
     * @return 推送结果
     */
    Result<Boolean> executePush(RuleCenterPushContext context);


}
