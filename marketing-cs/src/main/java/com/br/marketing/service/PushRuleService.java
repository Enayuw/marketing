package com.br.marketing.service;

import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.CustomerBatchNumDTO;
import com.br.marketing.vo.PushInfoDetailVO;
import com.br.marketing.vo.ScoreDetailVo;

import javax.validation.Valid;
import java.util.List;

public interface PushRuleService {

    /**
     * 获取批次信息
     * @param dto
     * @return
     */
    Result<List<ScoreDetailVo>> getBatchInfos(@Valid CustomerBatchNumDTO dto);

    /**
     * 获取任务推送记录
     */
    Result<List<PushInfoDetailVO>> getPushInfos(@Valid RequestPushInfoDTO dto);

    /**
     * 推送客服
     * @param dto
     * @return
     */
    Result<String> pushCustomer(@Valid PushCustomerDTO dto);
}
