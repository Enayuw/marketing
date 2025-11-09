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
     * 判断是否支持该策略
     *
     * @param dto 推送客户DTO
     * @return true-支持，false-不支持
     */
    boolean support(PushCustomerDTO dto);

    /**
     * 策略优先级，数字越小优先级越高
     * 上传任务优先级最高，其次是携程跑分，再次是合并跑分，最后是通用跑分
     *
     * @return 优先级值
     */
    int priority();
}

