package com.br.marketing.service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.dto.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.vo.MarketingPreUserSyncDetailVO;
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

    /**
     * 查询推送结果
     * @param mId
     * @return
     */
    Result<Boolean> getCustomerStatus(Long mId);

    /**
     * 批量推送人员
     * @param dto
     * @return
     */
    Result insertMarketingPreUser(@Valid RequestCommonDTO<MarketingPreUserDTO> dto);

    /**
     * 接受异步推送人员文本信息
     * @param apiCode
     * @param jsonData
     * @return
     */
    Result insertMarketingPreUserText(String apiCode,String jsonData);

    /**
     * 消费异步推送人员信息
     * @param infoId
     * @return
     */
    Result<Boolean> insertMarketingPreUserSync(Long infoId);


    Result<MarketingPreUserSyncDetailVO> getMarketingPreUserSyncStatus(@Valid MarketingPreUserSyncStatusDTO dto);
}
