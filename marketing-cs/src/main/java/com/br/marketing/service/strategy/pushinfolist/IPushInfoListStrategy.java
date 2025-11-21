package com.br.marketing.service.strategy.pushinfolist;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;

/**
 * 推送信息列表查询策略接口
 *
 * @author system
 * @date 2025-11-09
 */
public interface IPushInfoListStrategy {

    /**
     * 执行查询推送信息列表
     *
     * @param dto 查询条件DTO
     * @return 分页结果
     */
    PageResultReturn execute(PushInfoFilterDTO dto);
}

