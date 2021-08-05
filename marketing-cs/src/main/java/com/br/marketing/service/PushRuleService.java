package com.br.marketing.service;

import com.br.marketing.dto.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.vo.MarketingPreUserSyncDetailVO;
import com.br.marketing.vo.PushInfoDetailVO;
import com.br.marketing.vo.ScoreDetailVo;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;

public interface PushRuleService {

    /**
     * 获取批次信息
     *
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
     *
     * @param dto
     * @return
     */
    Result<String> pushCustomer(@Valid PushCustomerDTO dto);

    Result<Boolean> consumerPushCustomer(Long id);

    /**
     * 查询推送结果
     *
     * @param mId
     * @return
     */
    Result<Boolean> getCustomerStatus(Long mId);


    /**
     * 接受异步推送人员文本信息
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    Result insertMarketingPreUserText(String apiCode, String jsonData);


    Result insertBatchTransferUser(String apiCode,String jsonData);
    /**
     * 消费异步推送人员信息
     *
     * @param infoId
     * @return
     */
    Result<Boolean> insertMarketingPreUserSync(Long infoId);


    /**
     * 获取营销人员数据状态
     *
     * @param dto
     * @return
     */
    Result<MarketingPreUserSyncDetailVO> getMarketingPreUserSyncStatus(@Valid MarketingPreUserSyncStatusDTO dto);



    /**
     * 查询客户信息接口
     *
     * @param cid
     * @param custNum
     * @return
     */
    Result<MarketingSyncUser> queryCustInfo(String cid, String custNum);
}
