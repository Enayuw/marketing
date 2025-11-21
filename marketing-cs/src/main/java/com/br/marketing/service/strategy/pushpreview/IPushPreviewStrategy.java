package com.br.marketing.service.strategy.pushpreview;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.vo.xiecheng.PushViewVO;

/**
 * 推送预览策略接口
 * 
 * @author system
 * @date 2025-11-09
 */
public interface IPushPreviewStrategy {

    /**
     * 执行推送预览
     *
     * @param dto 推送客户DTO
     * @return 推送预览结果
     */
    Result<PushViewVO> execute(PushCustomerDTO dto);

    /**
     * 获取策略类型
     *
     * @return 策略类型枚举
     */
    PushPreviewStrategyEnum getStrategyType();
}

