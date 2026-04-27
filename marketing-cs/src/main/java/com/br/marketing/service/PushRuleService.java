package com.br.marketing.service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.ConditionSaveDTO;
import com.br.marketing.dto.CustomerBatchNumDTO;
import com.br.marketing.dto.MarketingPreUserSyncStatusDTO;
import com.br.marketing.dto.OptConditionDTO;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.rulecenter.XcCycleDeleteDTO;
import com.br.marketing.dto.rulecenter.XcCycleDeleteNumDTO;
import com.br.marketing.dto.rulecenter.XcDeleteMagnitudeDistDTO;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.CustomerQueueEnum;
import com.br.marketing.vo.*;
import com.br.marketing.vo.xiecheng.PushViewVO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface PushRuleService {


    /**
     * 查询apiCode对应的公司信息
     * @param apiCode apiCode
     * @return com.br.marketing.common.commondto.Result<Map<java.lang.String,java.lang.Object>> 响应结果
     */
    Result<Map<String,Object>> getCompanyAndModule(String apiCode);

    /**
     * 获取跑分批次号对应的场景
     * @param apiCode apiCode
     * @return com.br.marketing.common.commondto.Result<java.lang.String> 响应结果
     */
    Result<String> getUserType(String apiCode);

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
    Long getBatchInfosCounts(@Valid CustomerBatchNumDTO dto);

    /**
     * 获取任务推送记录
     */
    Result<List<PushInfoDetailVO>> getPushInfos(@Valid RequestPushInfoDTO dto);

    Result<CustomerInfoPushMain> getPushTask();

    Result isCanPushTask(Long taskId);
    /**
     * 推送客服
     *
     * @param dto
     * @return
     */
    Result<String> pushCustomer(@Valid PushCustomerDTO dto);

    Result<PushViewVO> pushPreview(@Valid PushCustomerDTO dto);

    String encrypt3k(Integer type, String content);

    Result<Long> saveCondition(@Valid ConditionSaveDTO dto);

    Result<List<ConditionOfScoreVO>> getConditionByRule(@Valid @NotNull(message = "apiCode不能为空")String apiCode, String name);

    Result<PageResultReturn<ScoreConditionDetailVO>> getConditionPageData(@Valid SearchConditionDTO dto);

    Result optCondition(@Valid OptConditionDTO dto);

    Result<Boolean> consumerPushCustomer(Long id);

    /**
     * 查询推送结果
     *
     * @param customerInfoPushMain
     * @return
     */
    Result<Boolean> getCustomerStatus(CustomerInfoPushMain customerInfoPushMain);


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

    Result<Boolean> consumerSyncInfo(String msg);

    /**
     * 插入转化数据
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    Result insertTransferData(String apiCode, String jsonData);

    Result insertTransferData(String apiCode, String jsonData, TransferDataDTO transferDataDTO);

    Result<Boolean> consumerTransferInfo(String msg);

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
    Result<MarketingSyncUser> queryCustInfo(String cid, String apiCode, String custNum, String cell);


    /**
     * 异步消费接口转化数据推送至客服 私人订制
     *
     * @param infoId 客户转化基础信息id
     * @return Result
     * @author Guo Zeqiang
     * @dateTime 2021/10/13 10:53
     */
    Result<Boolean> pushPersonalTransferData(Long infoId);

    Result<Boolean> pushPersonalTransferDataWrapper(String msg);

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

    Result<Boolean> consumerBlack(String msg);

    /**
     * 模拟数据库或者redis异常
     * @param mockType 1-数据库异常；2-redis异常
     * @param apiCode
     */
    void mockDbOrRedisError(Integer mockType,String apiCode);

    Result<Boolean> cunsumerZhongBangLabelData(Long id);

    Result<Integer> checkThreekEnc(List<Long> fileIds);

    Result collidingDataDelete(PushCustomerDTO dto);

    Result collidingDataCycleDelete(XcCycleDeleteDTO dto);

    Result collidingDataPachageMake(PushCustomerDTO dto);

    Result<Integer> collidingDataDeleteNum(PushCustomerDTO dto);

    Result<List<XcDeleteMagnitudeDistDTO>> collidingDataCycleDeleteMagnitudeDist(XcCycleDeleteNumDTO dto);

    void sendToMqByConfig(String apiCode, String defaultRoutingKey, String infoId, CustomerQueueEnum queueEnum);
    void sendToRocketMqByConfig(String apiCode, String topic, String tag, String infoId, CustomerQueueEnum queueEnum);

    Result<Boolean> deleteRule(Long id);
    Result<PushViewVO> queryFederation(PushCustomerDTO dto, PushViewVO pushViewVO);

    String getRoutingKeyFromRedis(String key, String field, String defaultValue);

    void judgeEncryptType(PushMarketingUserDetailByRuleDTO pushData, MarketingSyncUser syncUser, Integer jc3keyType);

    void processSensitiveInfo(JSONObject jsonObject, MarketingSyncUser syncUser, Integer jc3keyType);

    void sendJsonParseMq(String apiCode, String syncInfoId, Integer dataSourceType);

    void sendJsonParseMq(String apiCode, Long id, Integer dataSourceType, Integer dataType, Integer acceptType);

    Result<String> queryUploadOverAmt(String custNum, HttpServletRequest request);

    Result<MarketingSyncUserVO> queryLatestSyncUser(String apiCode, String custNum, String userType);

    void sendJsonParseMq(String apiCode,Integer dataSourceType,Integer systemType,Integer dataType,Integer acceptType,String jsonData);
    Result<List<ConditionVO>> getConditionList(String apiCode, String content);

    Result<ConditionVO> getConditionById(String apiCode, Long conditionId);
}
