package com.br.marketing.service;

import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.CustomerBatchNumDTO;
import com.br.marketing.vo.PushInfoDetailVO;
import com.br.marketing.vo.ScoreDetailVo;

import java.util.List;

public interface PushRuleService {

    /**
     * 获取批次信息
     * @param dto
     * @return
     */
    Result<List<ScoreDetailVo>> getBatchInfos(CustomerBatchNumDTO dto);

    /**
     * 获取任务推送记录
     */
    Result<List<PushInfoDetailVO>> getPushInfos(RequestPushInfoDTO dto);
}
