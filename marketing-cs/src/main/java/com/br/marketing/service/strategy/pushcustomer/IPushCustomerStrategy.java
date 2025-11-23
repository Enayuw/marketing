package com.br.marketing.service.strategy.pushcustomer;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.service.strategy.pushpreview.PushPreviewStrategyEnum;

/**
 * 推送客户策略接口
 *
 * @author system
 * @date 2025-11-09
 */
public interface IPushCustomerStrategy {

    /**
     * 执行推送客户
     *
     * @param dto 推送客户DTO
     * @return 推送结果，返回任务ID
     */
    Result<String> execute(PushCustomerDTO dto);

    /**
     * 获取策略类型（复用推送预览的枚举）
     *
     * @return 策略类型枚举
     */
    PushPreviewStrategyEnum getStrategyType();
}

