package com.br.marketing.service;

import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.*;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.vo.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface PushRuleService {



    Result<Map<String,Object>> getCompanyAndModule(String apiCode);

    /**
     * 获取批次信息
     *
     * @param dto
     * @return
     */
    PageResultReturn getBatchInfos(@Valid CustomerBatchNumDTO dto);

    /**
     * 获取批次列表跑分总数
     * @param dto
     * @return
     */
    Integer getBatchInfosCounts(@Valid CustomerBatchNumDTO dto);

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

    Result<Integer> pushPreview(@Valid PushCustomerDTO dto);

    String encrypt3k(Integer type, String content);

    Result<Long> saveCondition(@Valid ConditionSaveDTO dto);

    Result<List<ConditionOfScoreVO>> getConditionByRule(@Valid @NotNull(message = "apiCode不能为空")String apiCode, String name);

    Result<PageResultReturn<ScoreConditionDetailVO>> getConditionPageData(@Valid SearchConditionDTO dto);

    Result optCondition(@Valid OptConditionDTO dto);

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


    Result insertBatchTransferUser(String apiCode, String jsonData);

    /**
     * 消费异步推送人员信息
     *
     * @param infoId
     * @return
     */
    Result<Boolean> insertMarketingPreUserSync(Long infoId);

    /**
     * 插入转化数据
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    Result insertTransferData(String apiCode, String jsonData);


    Result consumerTransferData(Long id);

    Result<MarketingTransferUserStatusVO> getTransferDataStatus(String apiCode, String requestId);

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
     * @param apiCode
     * @param custNum
     * @return
     */
    Result<MarketingSyncUser> queryCustInfo(String cid, String apiCode, String custNum);


    /**
     * 异步消费接口转化数据推送至客服 私人订制
     *
     * @param infoId 客户转化基础信息id
     * @return Result
     * @author Guo Zeqiang
     * @dateTime 2021/10/13 10:53
     */
    Result<Boolean> pushPersonalTransferData(Long infoId);

    /**
     * 异步消费接口转化数据推送至客服 通用
     *
     * @param transferInfo 客户转化基础信息
     * @author Guo Zeqiang
     * @dateTime 2021/11/4 10:53
     */
    List<TransferRobotOutboundVO<UnsuccessfulData>> pushTransferData(MarketingTransferInfo transferInfo);

    /**
     * 异步消费接口转化数据推送至客服 通用
     *
     * @param dto 推送数据
     * @author Guo Zeqiang
     * @dateTime 2021/11/4 10:53
     */
    TransferRobotOutboundVO<UnsuccessfulData> pushTransferData(TransferRobotOutboundDTO dto, MarketingTransferInfo transferInfo);

    /**
     * 获取推送数据
     *
     * @author Guo Zeqiang
     * @dateTime 2021/11/4 10:53
     */
    TransferRobotOutboundDTO getTransferRobotOutbound(MarketingTransferInfo transferInfo, List<MarketingTransferSyncUser> transferList);

    Result<Boolean> consumerCommonBlack(Long id);

    Result<Boolean> consumerBlack(Long id);

    Result<Boolean> consumerHaLuo(Long id);
}
