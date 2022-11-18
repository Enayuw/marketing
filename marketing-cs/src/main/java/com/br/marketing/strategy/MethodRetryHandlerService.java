package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.br.marketing.bo.ZaMarketDataBO;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.PushBlackListResponse;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportAdapHaluoDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.black.BlackListDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.output.DassExportAdapterDTO;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneParentDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.ReqBlackPhoneVO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.DataCompare;
import com.br.marketing.entity.PhoneSaleExtendHaluo;
import com.br.marketing.entity.PhoneSaleExtendHaluoExample;
import com.br.marketing.mapper.*;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description :处理三方接口重试逻辑处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/25 11:13
 */

@Service
@Slf4j
public class MethodRetryHandlerService {

    @Resource
    private DassServiceClient dassServiceClient;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private DataCompareMapper dataCompareMapper;

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    PhoneSaleExtendHaluoMapper phoneSaleExtendHaluoMapper;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Resource
    private ZhongAnClient zhongAnClient;

    @Resource
    private ZhonganRosterLockingDataMapper zhonganRosterLockingDataMapper;

    /**
     * 全局重试任务执行类
     *
     * @param dassExportAdapterDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result<PushBlackListResponse> callBlackList(DassExportAdapterDTO dassExportAdapterDTO, Integer retry) {
        List<BlackListDTO> list = dassExportAdapterDTO.getList();
        Result<PushBlackListResponse> pushBlackListResponseResult = dassServiceClient.postBlackList(list);
        // 调用接口成功
        if (ResultCode.SUCCESS.getValue().equals(pushBlackListResponseResult.getCode())){
            // 1、保存业务调用日志，留存数据id到数据库
            Set<String> set = list.stream().map(BlackListDTO::getDataId).collect(Collectors.toSet());
            saveBizLog(String.join(",",set),InterfaceHandlerEnum.ARTIFICIAL_BLACK_LIST.getCode(),dassExportAdapterDTO.getTransferInfoId());

            //2、所有失效数据需要修改上传详情表数据库状态
            Map<String, Set<String>> collect = list.stream().collect(Collectors.groupingBy(BlackListDTO::getApiCode,
                    Collectors.mapping(BlackListDTO::getUid, toSet())));
            Set<Map.Entry<String, Set<String>>> entries = collect.entrySet();
            for (Map.Entry<String, Set<String>> entry : entries) {
                marketingSyncUserMapper.updateSyncUserCaseEffective(entry.getKey(),entry.getValue());
            }
            return pushBlackListResponseResult.setCode(ResultCode.SUCCESS.getValue());
        }
        log.error("调用人工黑名单失败 -- {}", JSON.toJSONString(pushBlackListResponseResult));
        return pushBlackListResponseResult.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    /**
     * 调用Dass接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     *
     * @param dassImportAdapDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result callDassRealTimeUserData(DassSingleImportAdapDTO dassImportAdapDTO, Integer retry) {

        Result result = dassServiceClient.postRealTimeUserData(dassImportAdapDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            saveBizLog(dassImportAdapDTO.getExtendInfo(), InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA.getCode(),
                    dassImportAdapDTO.getTransferInfoId());
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.error("调用人工实时推送用户名单失败 -- {}", JSON.toJSONString(result));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    /**
     * 调用客服黑名单接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     *
     * @param parentDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result<String> callCustomerBlack(ReqBlackPhoneParentDTO parentDTO, Integer retry){
        ReqBlackPhoneVO reqBlackPhoneVO = robotaiApiServiceClient.pushBlack(parentDTO);
        if ("00".equals(reqBlackPhoneVO.getCode()) && CollectionUtils.isEmpty(reqBlackPhoneVO.getData())) {
            Set<String> set = parentDTO.getBlackDetailDTOList().stream().map(BlackDetailDTO::getDataId).collect(Collectors.toSet());
            saveBizLog(String.join(",", set), InterfaceHandlerEnum.CUSTOMER_BLACK_LIST.getCode(), parentDTO.getTransferInfoId());
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        if (("00".equals(reqBlackPhoneVO.getCode()) && (!CollectionUtils.isEmpty(reqBlackPhoneVO.getData())))
                || "9999".equals(reqBlackPhoneVO.getCode())) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue())
                    .setDate("9999".equals(reqBlackPhoneVO.getCode()) ? "9999" : "部分成功");
        }
        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(reqBlackPhoneVO.getCode());
    }

    /**
     * 调用客户接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     * @param robotOutboundDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result<TransferRobotOutboundVO<UnsuccessfulData>> callCustomerTransfer(TransferRobotOutboundDTO robotOutboundDTO, Integer retry){
        TransferRobotOutboundVO<UnsuccessfulData> transferRobotOutboundVO = robotaiApiServiceClient.pushRobotai(robotOutboundDTO);
        if (!"9999".equals(transferRobotOutboundVO.getCode())){
            List<ConversionData> conversionData = robotOutboundDTO.getJsonData().getConversionData();
            Set<String> set = conversionData.stream().map(ConversionData::getDataId).collect(Collectors.toSet());
            saveBizLog(String.join(",",set),InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode(),robotOutboundDTO.getTransferInfoId());
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(transferRobotOutboundVO);
        }
        log.error("调用客服接口失败 -- {}",JSON.toJSONString(transferRobotOutboundVO));
        //调用客户转化接口失败，记录数据入库，定时任务重试
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(transferRobotOutboundVO);
    }

    void saveBizLog(String data, Integer handlerEnum, Long infoId) {
        DataCompare dataCompare = new DataCompare(data, handlerEnum, infoId);
        dataCompareMapper.insertSelective(dataCompare);
    }


    /**
     * 调用电销批量接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     * @param dassImportAdapDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result callDassRealTimeBatchData(DassImportAdapDTO dassImportAdapDTO, Integer retry) {
        Result result = dassServiceClient.postHermesUserData(dassImportAdapDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            Set<String> set = dassImportAdapDTO.getList().stream().map(DassImportDataDTO::getId).map(String::valueOf).collect(Collectors.toSet());
            saveBizLog(String.join(",",set), InterfaceHandlerEnum.ARTIFICIAL_BATCH_REALTIME_DATA.getCode(),
                    dassImportAdapDTO.getTransferInfoId());
            phoneSaleExtendInfoMapper.updateBatch(set);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.error("调用批量人工实时转电销失败 -- {}", JSON.toJSONString(result));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }


    /**
     * 调用电销批量接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     * @param dassImportAdapDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result callDassRealTimeBatchData(DassImportAdapHaluoDTO dassImportAdapDTO, Integer retry) {
        Result result = dassServiceClient.postHermesUserData(dassImportAdapDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            if(dassImportAdapDTO.getIsJob().equals(new Integer(0))) {
                Set<String> set = dassImportAdapDTO.getPhoneSaleExtendHaluos()
                        .stream().map(PhoneSaleExtendHaluo::getSourceId)
                        .map(String::valueOf)
                        .collect(Collectors.toSet());
                saveBizLog(String.join(",", set), InterfaceHandlerEnum.ARTIFICIAL_BATCH_REALTIME_DATA.getCode(),
                        dassImportAdapDTO.getTransferInfoId());
            }
            List<Long> ids = dassImportAdapDTO.getPhoneSaleExtendHaluos()
                    .stream().map(PhoneSaleExtendHaluo::getId)
                    .collect(Collectors.toList());
            PhoneSaleExtendHaluoExample updateExample = new PhoneSaleExtendHaluoExample();
            updateExample.createCriteria().andIdIn(ids);
            PhoneSaleExtendHaluo updateEntity = new PhoneSaleExtendHaluo();
            updateEntity.setpStatus(2);
            phoneSaleExtendHaluoMapper.updateByExampleSelective(updateEntity,updateExample);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.error("调用批量人工实时转电销失败 -- {}", JSON.toJSONString(result));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }


    /**
     * 调用电销转化接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     * @param dassTransferDataAdapDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result callDassTransferData(DassTransferDataAdapDTO dassTransferDataAdapDTO, Integer retry) {
        Result result = dassServiceClient.postTransferData(dassTransferDataAdapDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            Set<String> set = dassTransferDataAdapDTO.getDassTransferDataDTOList().stream().map(DassTransferDataDTO::getId).map(String::valueOf).collect(Collectors.toSet());
            saveBizLog(String.join(",",set), InterfaceHandlerEnum.ARTIFICIAL_TRANSFER.getCode(),
                    dassTransferDataAdapDTO.getTransferInfoId());
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.error("调用电销转化接口失败 -- {}", JSON.toJSONString(result));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    /**
     * 推送决策接口
     * @param dto
     * @param retry
     * @return
     */
    @RetryMethod(retryNowNum = 2,isOrNoDbRetry = true)
    public Result callPolicyData(PolicyRetryByRuleDTO dto, Integer retry){
        List<Long> ids = dto.getIds();
        PushMarketingUserDTO pushMarketingUserDTO = dto.getPushMarketingUserDTO();
        Long infoId = dto.getInfoId();
        Result result = intelligentCustomerServiceClient.pushUser(pushMarketingUserDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            saveBizLog(Joiner.on(",").join(ids), InterfaceHandlerEnum.INIT_TO_POLICY.getCode(), infoId);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.error("调用推送决策接口失败 -- {}", JSON.toJSONString(result));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    /**
     * 推送众安接口
     *
     * @param bo    封装的数据
     * @param retry 是否重试
     */
    @RetryMethod(retryNowNum = 1, isOrNoDbRetry = true)
    public Result<?> callZhongAnData(ZaMarketDataBO bo, Integer retry) {
        Result<Object> result = new Result<>();
        Result zhongAnResult = zhongAnClient.pushDetail(bo.getDataDTO());
        switch (zhongAnResult.getCode()) {
            case 500:
                // 已推送,未成功,需要重试
                updatePushStatus(bo, 3, 1);
                break;
            case 0:
                // 已推送,未成功,无需重试
                updatePushStatus(bo, 4, null);
                break;
            default:
                updatePushStatus(bo, 2, null);
        }
        result.setCode(zhongAnResult.getCode());
        return result;
    }

    private void updatePushStatus(ZaMarketDataBO bo, Integer updatePushStatus, Integer updateStatus) {
        zhonganRosterLockingDataMapper.updatePushStatusORStatus(bo.getApiCode(), updatePushStatus, updateStatus
                , null, bo.getTag(), bo.getList(), LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

}
