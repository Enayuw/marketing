package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.aspect.Mockable;
import com.br.marketing.bo.SaveReachDeleteRecordReqBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.bo.ZaMarketDataBO;
import com.br.marketing.client.HaloCallBackDataApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.PushBlackListResponse;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportAdapHaluoDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.IbuReqDTO;
import com.br.marketing.client.dassservice.input.black.BlackListDTO;
import com.br.marketing.client.dassservice.input.transfer.DassAssembleTransferDataSoleDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapSoleDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.client.dassservice.output.DassExportAdapterDTO;
import com.br.marketing.client.didi.DiDiClient;
import com.br.marketing.client.didi.input.DiDiReachBO;
import com.br.marketing.client.didi.input.DiDiReachRequestTO;
import com.br.marketing.client.didi.input.DiDiReqVO;
import com.br.marketing.client.didi.output.DiDiResponseTO;
import com.br.marketing.client.guomei.GmCallBackResponse;
import com.br.marketing.client.guomei.GuoMeiClient;
import com.br.marketing.client.guomei.result.GmMarketingResultCallBackRequest;
import com.br.marketing.client.guomei.userdata.GmUserDataCallBackRequest;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleSoleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.client.qifu.QiFuClients;
import com.br.marketing.client.qifu.ResponseData;
import com.br.marketing.client.qifu.SaveReachDeleteRecordReq;
import com.br.marketing.client.qifu.SaveReachDeleteRecordResp;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeReq;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeResp;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.*;
import com.br.marketing.client.robotaiapi.output.ReqBlackPhoneVO;
import com.br.marketing.client.robotaiapi.output.RobotOutboundVo;
import com.br.marketing.client.robotaiapi.output.TransferRobotDataVO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.smy.SmyClient;
import com.br.marketing.client.smy.input.SmyCommReqDto;
import com.br.marketing.client.zbank.ZbankClient;
import com.br.marketing.client.zbank.ZbankResponse;
import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.common.annoation.DistributeLog;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.constants.MockConstants;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.dto.zbank.ZbankAIVoiceFileDetailResultDTO;
import com.br.marketing.dto.zbank.ZbankLabelRatingReResultDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.DiDiAllowMarketingEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.Impl.PushRuleServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.DiDiAllowReqDTO;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Resource
    private ZhongAnSmsRosterLockingDataMapper zhongAnSmsRosterLockingDataMapper;

    @Resource
    ZhonganMarketingBanMapper zhonganMarketingBanMapper;

    @Resource
    PhoneSaleTransferMapper phoneSaleTransferMapper;

    @Resource
    PhoneSaleIbuMapper phoneSaleIbuMapper;

    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    DidiDataMapper didiDataMapper;

    @Resource
    private DiDiClient diDiClient;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private DidiCallRecordMapper didiCallRecordMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private QiFuClients qiFuClients;

    @Resource
    private QifuSaveReachDeleteRecordApiPushLogMapper qifuSaveReachDeleteRecordApiPushLogMapper;

    @Resource
    private ZbankClient zBankClient;

    @Resource
    private PushRuleServiceImpl pushRuleService;

    @Resource
    private GuoMeiClient guoMeiClient;

    @Resource
    private SmyClient smyClient;


    /**
     * 渠道唯一标识（由众邦银行提供）
     */
    @Value("${api.zbank.file.channelId:2023042701}")
    private String channelId;

    /**
     * @param data                     数据
     * @param distributeTypeEnum       DistributeTypeEnum 数据流向枚举
     * @param apiCode
     * @param custNum                  案件号
     * @param cell                     手机号
     * @param sourceId                 源数据id
     * @param distributeSourceTypeEnum 数据源类型
     * @return
     */
    public DataJoinLogDTO dataJoinLogFix(Object data, DistributeTypeEnum distributeTypeEnum, String apiCode
            , String custNum, String cell, Long sourceId, DistributeSourceTypeEnum distributeSourceTypeEnum, String status, String extend) {
        DataJoinLogDTO dataJoinLogDTO = new DataJoinLogDTO();
        dataJoinLogDTO.setApiCode(apiCode);
        dataJoinLogDTO.setCustNum(custNum);
        dataJoinLogDTO.setCell(cell);
        dataJoinLogDTO.setDistributeDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dataJoinLogDTO.setDistributeType(distributeTypeEnum.getValue());
        dataJoinLogDTO.setCreateTime(new Date());
        dataJoinLogDTO.setSourceId(sourceId);
        dataJoinLogDTO.setSourceType(distributeSourceTypeEnum.getValue());
        dataJoinLogDTO.setDataCode(data.hashCode());
        dataJoinLogDTO.setDataMd5(DigestUtils.md5DigestAsHex(data.toString().getBytes()));
        dataJoinLogDTO.setStatus(status);
        dataJoinLogDTO.setExtend(extend);
        return dataJoinLogDTO;
    }

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
        if (ResultCode.SUCCESS.getValue().equals(pushBlackListResponseResult.getCode())) {
            // 1、保存业务调用日志，留存数据id到数据库
            Set<String> set = list.stream().map(BlackListDTO::getDataId).collect(Collectors.toSet());
            saveBizLog(String.join(",", set), InterfaceHandlerEnum.ARTIFICIAL_BLACK_LIST.getCode(), dassExportAdapterDTO.getTransferInfoId());

            //2、所有失效数据需要修改上传详情表数据库状态
            Map<String, Set<String>> collect = list.stream().collect(Collectors.groupingBy(BlackListDTO::getApiCode,
                    Collectors.mapping(BlackListDTO::getUid, toSet())));
            Set<Map.Entry<String, Set<String>>> entries = collect.entrySet();
            for (Map.Entry<String, Set<String>> entry : entries) {
                marketingSyncUserMapper.updateSyncUserCaseEffective(entry.getKey(), entry.getValue());
            }
            return pushBlackListResponseResult.setCode(ResultCode.SUCCESS.getValue());
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                , "调用人工黑名单失败 -- " + JSON.toJSONString(pushBlackListResponseResult)));
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
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                , "调用人工实时推送用户名单失败 -- " + JSON.toJSONString(result)));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    /**
     * 2023-08-24 13:28
     * 灵明石猴
     * 人工实时推送用户名单(单条)处理，带去重的方法
     * 与callDassRealTimeUserData方法逻辑一毛一样
     */
    @DistributeLog
    @RetryMethod(isOrNoDbRetry = true)
    public Result<JSONObject> callDassRealTimeUserDataSole(DassSingleImportAdapSoleDTO dassImportAdapDTO, Integer retry
            , PhoneSaleExtendInfo phoneSaleExtendInfo) {
        List<DassSingleImportDataDTO> data = dassImportAdapDTO.getData();
        if (CollectionUtils.isEmpty(data)) {
            Result<JSONObject> result = new Result<>();
            result.setCode(ResultCode.SUCCESS.getValue());
            result.setMessage("去重后，数据为空");
            return result;
        }
        //插入b_phone_sale_extend_info
        if (phoneSaleExtendInfo != null) {
            try {
                phoneSaleExtendInfo.setCreateTime(new Date());
                phoneSaleExtendInfoMapper.insertSelective(phoneSaleExtendInfo);
                dassImportAdapDTO.setExtendInfo(phoneSaleExtendInfo.getId().toString());
            } catch (Exception e) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), e.getMessage()), e);
            }
        }
        DassSingleImportAdapDTO dassSingleImportAdapDTO = new DassSingleImportAdapDTO();
        dassSingleImportAdapDTO.setDassSingleImportDataDTO(dassImportAdapDTO.getDassSingleImportDataDTO());
        dassSingleImportAdapDTO.setExtendInfo(dassImportAdapDTO.getExtendInfo());
        dassSingleImportAdapDTO.setTransferInfoId(dassImportAdapDTO.getTransferInfoId());
        Result<JSONObject> result = dassServiceClient.postRealTimeUserData(dassSingleImportAdapDTO);
        PhoneSaleExtendInfo info = new PhoneSaleExtendInfo();
        info.setId(Long.valueOf(dassImportAdapDTO.getExtendInfo()));
        info.setUpdateTime(new Date());
        info.setPushDxTime(new Date());
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            saveBizLog(dassImportAdapDTO.getExtendInfo(), dassImportAdapDTO.getInterfaceHandlerEnum() == null
                            ? InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA_SOLE.getCode()
                            : dassImportAdapDTO.getInterfaceHandlerEnum().getCode(),
                    dassImportAdapDTO.getTransferInfoId());
            info.setPStatus(2);
            phoneSaleExtendInfoMapper.updateByPrimaryKeySelective(info);
            return result;
        }
        info.setPStatus(3);
        phoneSaleExtendInfoMapper.updateByPrimaryKeySelective(info);
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                , "调用人工实时推送用户名单失败 -- " + JSON.toJSONString(result)));
        result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        return result;
    }

    /**
     * 2023-09-02 14:22
     * 六耳猕猴
     */
    @DistributeLog
    public Result<JSONObject> callDassRealTimeUserDataSole(DassSingleImportAdapSoleDTO dassImportAdapDTO, Integer retry) {
        return callDassRealTimeUserDataSole(dassImportAdapDTO, retry, null);
    }

    /**
     * 调用Dass接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     *
     * @param realTimeUserDataDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result callDassRealTimeLog(RealTimeUserDataDTO realTimeUserDataDTO, Integer retry) {

        Result<JSONObject> result = dassServiceClient.postRealTimeUserData(realTimeUserDataDTO.getDassSingleImportAdapDTO());
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            Integer code = result.getData().getInteger("code");
            if (new Integer(0).equals(code)) {
                saveBizLog(realTimeUserDataDTO.getDassSingleImportAdapDTO().getExtendInfo(), InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_LOG.getCode(),
                        realTimeUserDataDTO.getDassSingleImportAdapDTO().getTransferInfoId());
                PhoneSaleExtendInfo phoneSaleExtendInfo = realTimeUserDataDTO.getPhoneSaleExtendInfo();
                PhoneSaleExtendInfo update = new PhoneSaleExtendInfo();
                update.setPushDxTime(new Date());
                update.setId(phoneSaleExtendInfo.getId());
                update.setPStatus(2);
                phoneSaleExtendInfoMapper.updateByPrimaryKeySelective(update);
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            } else {
                PhoneSaleExtendInfo phoneSaleExtendInfo = realTimeUserDataDTO.getPhoneSaleExtendInfo();
                PhoneSaleExtendInfo update = new PhoneSaleExtendInfo();
                update.setPushDxTime(new Date());
                update.setId(phoneSaleExtendInfo.getId());
                update.setPStatus(3);
                phoneSaleExtendInfoMapper.updateByPrimaryKeySelective(update);
            }
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                , "调用人工实时推送用户名单失败 -- " + JSON.toJSONString(result)));
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
    public Result<String> callCustomerBlack(ReqBlackPhoneParentDTO parentDTO, Integer retry) {
        ReqBlackPhoneVO reqBlackPhoneVO = robotaiApiServiceClient.pushBlack(parentDTO);
        if ("00".equals(reqBlackPhoneVO.getCode()) && CollectionUtils.isEmpty(reqBlackPhoneVO.getData())) {
            if ("1".equals(parentDTO.getExtendInfo())) {
                List<Long> ids = parentDTO.getBlackDetailDTOList().stream().map(t -> Long.valueOf(t.getDataId())).collect(Collectors.toList());
                ZhonganMarketingBanExample example = new ZhonganMarketingBanExample();
                example.createCriteria().andIdIn(ids);
                ZhonganMarketingBan update = new ZhonganMarketingBan();
                update.setPushTime(new Date());
                update.setPushStatus(2);
                zhonganMarketingBanMapper.updateByExampleSelective(update, example);
            } else {
                Set<String> set = parentDTO.getBlackDetailDTOList().stream().map(BlackDetailDTO::getDataId).collect(Collectors.toSet());
                saveBizLog(String.join(",", set), InterfaceHandlerEnum.CUSTOMER_BLACK_LIST.getCode(), parentDTO.getTransferInfoId());
            }
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
//        if (("00".equals(reqBlackPhoneVO.getCode()) && (!CollectionUtils.isEmpty(reqBlackPhoneVO.getData())))
//                || "9999".equals(reqBlackPhoneVO.getCode())) {
//            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue())
//                    .setDate("9999".equals(reqBlackPhoneVO.getCode()) ? "9999" : "部分成功");
//        }
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(reqBlackPhoneVO.getCode());
    }

    @RetryMethod(retryNowNum = 3)
    public Result<String> callRobotOutbound(RobotOutboundGeneralDTO dto, Integer retry, String method) {
        RobotOutboundVo robotOutboundVo = robotaiApiServiceClient.pushRobotOutbound(dto, method);
        String code = robotOutboundVo.getCode();
        if ("00".equals(code)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode()
                , "调用客服接口失败，method：" + method
                        + "，result：" + JSON.toJSONString(robotOutboundVo) + " -- " + JSON.toJSONString(dto)));
        if ("900001".equals(code)) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        return new Result().setCode(ResultCode.FAIL.getValue());
    }

    /**
     * 调用客户接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     *
     * @param robotOutboundDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result<TransferRobotOutboundVO<TransferRobotDataVO>> callCustomerTransfer(TransferRobotOutboundDTO robotOutboundDTO, Integer retry) {
        TransferRobotOutboundVO<TransferRobotDataVO> transferRobotOutboundVO = robotaiApiServiceClient.pushRobotai(robotOutboundDTO);
        //重复请求900031，认为成功
        if ((!"9999".equals(transferRobotOutboundVO.getCode()) && getAllSuccessful(transferRobotOutboundVO)) ||
                "900031".equals(transferRobotOutboundVO.getCode())) {
            List<ConversionData> conversionData = robotOutboundDTO.getJsonData().getConversionData();
            Set<String> set = conversionData.stream().map(ConversionData::getDataId).collect(Collectors.toSet());
            saveBizLog(String.join(",", set), InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode(), robotOutboundDTO.getTransferInfoId());
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(transferRobotOutboundVO);
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode()
                , "调用客服接口失败 -- " + JSON.toJSONString(transferRobotOutboundVO)));
        //调用客户转化接口失败，记录数据入库，定时任务重试
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(transferRobotOutboundVO);
    }

    /**
     * 客户转化去重方法
     *
     * @param robotOutboundDTO
     * @param retry
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    @DistributeLog
    public Result<TransferRobotOutboundVO<TransferRobotDataVO>> callCustomerTransfer(TransferRobotOutboundSoleDTO robotOutboundDTO, Integer retry) {
        if (robotOutboundDTO.getData().size() <= 0) {
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }
        TransferRobotOutboundDTO transferRobotOutboundDTO = new TransferRobotOutboundDTO();
        transferRobotOutboundDTO.setTransferInfoId(robotOutboundDTO.getTransferInfoId());
        transferRobotOutboundDTO.setApiCode(robotOutboundDTO.getApiCode());
        transferRobotOutboundDTO.setJsonData(new TransferJsonDataDTO(robotOutboundDTO.getData(), robotOutboundDTO.getLast()));
        TransferRobotOutboundVO<TransferRobotDataVO> transferRobotOutboundVO = robotaiApiServiceClient.pushRobotai(transferRobotOutboundDTO);
        //重复请求900031，认为成功
        if ((!"9999".equals(transferRobotOutboundVO.getCode()) && getAllSuccessful(transferRobotOutboundVO)) ||
                "900031".equals(transferRobotOutboundVO.getCode())) {
            Set<Long> set = robotOutboundDTO.getDetailLogList().stream().map(DataDistributeDetailLog::getSourceId).collect(toSet());
            saveBizLog(Joiner.on(",").join(set), InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode(), robotOutboundDTO.getTransferInfoId());
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(transferRobotOutboundVO);
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode()
                , "调用客服接口失败 -- " + JSON.toJSONString(transferRobotOutboundVO)));
        //调用客户转化接口失败，记录数据入库，定时任务重试
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(transferRobotOutboundVO);
    }

    public Result<TransferRobotOutboundVO<TransferRobotDataVO>> xieChengSmsCallCustomerTransfer(TransferRobotOutboundDTO robotOutboundDTO, Integer retry) {
        TransferRobotOutboundVO<TransferRobotDataVO> transferRobotOutboundVO = robotaiApiServiceClient.pushRobotai(robotOutboundDTO);
        //重复请求900031，认为成功
        if ((!"9999".equals(transferRobotOutboundVO.getCode()) && getAllSuccessful(transferRobotOutboundVO)) ||
                "900031".equals(transferRobotOutboundVO.getCode())) {
            List<ConversionData> conversionData = robotOutboundDTO.getJsonData().getConversionData();
            Set<String> set = conversionData.stream().map(ConversionData::getDataId).collect(Collectors.toSet());
            DataCompare dataCompare = new DataCompare(String.join(",", set), InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode(), null);
            dataCompare.setRemark("xieChengSmsPushToTransfer");
            dataCompareMapper.insertSelective(dataCompare);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(transferRobotOutboundVO);
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode()
                , "携程新场景短信撞库，调用客服接口失败 --  -- " + JSON.toJSONString(transferRobotOutboundVO)));
        //调用客户转化接口失败，记录数据入库，定时任务重试
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(transferRobotOutboundVO);
    }

    private static Boolean getAllSuccessful(TransferRobotOutboundVO<TransferRobotDataVO> transferRobotOutboundVO) {
        // 当unsuccessfulData数组中有值时，需要重试
        Boolean allSuccessful = Boolean.FALSE;
        if (null != transferRobotOutboundVO.getData() && CollectionUtils.isEmpty(transferRobotOutboundVO.getData().getUnsuccessfulData())) {
            allSuccessful = Boolean.TRUE;
        }
        return allSuccessful;
    }

    void saveBizLog(String data, Integer handlerEnum, Long infoId) {
        DataCompare dataCompare = new DataCompare(data, handlerEnum, infoId);
        dataCompareMapper.insertSelective(dataCompare);
    }


    /**
     * 调用电销批量接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     *
     * @param dassImportAdapDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result callDassRealTimeBatchData(DassImportAdapDTO dassImportAdapDTO, Integer retry) {
        Result result = dassServiceClient.postHermesUserData(dassImportAdapDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            Set<String> set = dassImportAdapDTO.getList().stream().map(DassImportDataDTO::getId).map(String::valueOf).collect(Collectors.toSet());
            saveBizLog(String.join(",", set), InterfaceHandlerEnum.ARTIFICIAL_BATCH_REALTIME_DATA.getCode(),
                    dassImportAdapDTO.getTransferInfoId());
            phoneSaleExtendInfoMapper.updateBatch(set);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                , "调用批量人工实时转电销失败 -- " + JSON.toJSONString(result)));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }


    /**
     * 萨摩耶推daas
     *
     * @param dassImportAdapDTO
     * @param retry
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result smyCallDassRealTimeBatchData(DassImportAdapDTO dassImportAdapDTO, Integer retry) {
        Result result = dassServiceClient.postHermesUserData(dassImportAdapDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            Set<String> set = dassImportAdapDTO.getList().stream().map(DassImportDataDTO::getId).map(String::valueOf).collect(Collectors.toSet());
            phoneSaleExtendInfoMapper.updateBatch(set);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                , "转化数据周期推送电销失败 -- " + JSON.toJSONString(result)));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    /**
     * 调用电销批量接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     *
     * @param dassImportAdapDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result callDassRealTimeBatchData(DassImportAdapHaluoDTO dassImportAdapDTO, Integer retry) {
        Result result = dassServiceClient.postHermesUserData(dassImportAdapDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            if (dassImportAdapDTO.getIsJob().equals(new Integer(0))) {
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
            phoneSaleExtendHaluoMapper.updateByExampleSelective(updateEntity, updateExample);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                , "调用批量人工实时转电销失败 -- " + JSON.toJSONString(result)));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }


    /**
     * 调用电销转化接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     *
     * @param dassTransferDataAdapDTO
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result callDassTransferData(DassTransferDataAdapDTO dassTransferDataAdapDTO, Integer retry) {
        Result result = dassServiceClient.postTransferData(dassTransferDataAdapDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            Set<String> set = dassTransferDataAdapDTO.getDassTransferDataDTOList().stream().map(DassTransferDataDTO::getId).map(String::valueOf).collect(Collectors.toSet());
            saveBizLog(String.join(",", set), InterfaceHandlerEnum.ARTIFICIAL_TRANSFER.getCode(),
                    dassTransferDataAdapDTO.getTransferInfoId());
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                , "调用电销转化接口失败 -- " + JSON.toJSONString(result)));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    /**
     * 调用电销转化接口 带去重功能
     * 调用成功，将该批数据记录到数据库中以便数据对比
     */
    @RetryMethod(isOrNoDbRetry = true)
    @DistributeLog
    public Result<?> callDassTransferDataSole(DassTransferDataAdapSoleDTO dassTransferDataAdapDTO, Integer retry) {
        if (CollectionUtils.isEmpty(dassTransferDataAdapDTO.getData())) {
            Result<?> result = new Result<>();
            result.setCode(ResultCode.SUCCESS.getValue());
            result.setMessage("去重后，数据为空");
            return result;
        }
        List<DassTransferDataDTO> list = dassTransferDataAdapDTO.getData().stream().map(
                DassAssembleTransferDataSoleDTO::getDassTransferDataDTO).collect(Collectors.toList());
        DassTransferDataAdapDTO dto = new DassTransferDataAdapDTO();
        dto.setDassTransferDataDTOList(list);
        dto.setTransferInfoId(dassTransferDataAdapDTO.getTransferInfoId());
        dto.setPhoneSaleExtendInfoList(dassTransferDataAdapDTO.getPhoneSaleExtendInfoList());
        Result<?> result = dassServiceClient.postTransferData(dto);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            Set<String> set = list.stream().map(DassTransferDataDTO::getId).map(String::valueOf).collect(Collectors.toSet());
            saveBizLog(String.join(",", set), dassTransferDataAdapDTO.getInterfaceHandlerEnum() == null
                            ? InterfaceHandlerEnum.ARTIFICIAL_TRANSFER_SOLE.getCode()
                            : dassTransferDataAdapDTO.getInterfaceHandlerEnum().getCode(),
                    dassTransferDataAdapDTO.getTransferInfoId());
            return result;
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                , "调用电销去重转化接口失败 -- " + JSON.toJSONString(result)));
        result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        return result;
    }


    /**
     * 萨摩耶转化数据剔除
     *
     * @param dassTransferDataAdapDTO
     * @param retry
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result smyCallDassTransferData(DassTransferDataAdapDTO dassTransferDataAdapDTO, Integer retry) {
        Result result = dassServiceClient.postTransferData(dassTransferDataAdapDTO);
        log.warn("萨摩耶调用电销转化接口返回结果 -- {}", JSON.toJSONString(result));
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                , "萨摩耶调用电销转化接口失败 -- " + JSON.toJSONString(result)));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    @RetryMethod(isOrNoDbRetry = true)
    public Result dassTransferWithFile(DassTransferDataAdapDTO dassTransferDataAdapDTO, Integer retry) {
        Result result = dassServiceClient.postTransferData(dassTransferDataAdapDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            List<Long> ids = dassTransferDataAdapDTO.getDassTransferDataDTOList().stream().map(t -> t.getId()).collect(Collectors.toList());
            PhoneSaleTransfer updateEntity = new PhoneSaleTransfer();
            JSONObject jsonObject = JSON.parseObject(result.getData().toString());
            boolean code = "0".equals(jsonObject.getString("code"));
            if (new Integer(1).equals(retry)) {
                Long id = ids.get(0);
                PhoneSaleTransfer phoneSaleTransfer = phoneSaleTransferMapper.selectByPrimaryKey(id);
                LocalFile localFile = localFileMapper.selectByPrimaryKey(Long.valueOf(phoneSaleTransfer.getLocalId()));
                LocalFile updateFile = new LocalFile();
                updateFile.setId(localFile.getId());
                if (code) {
                    updateFile.setPushNumber(localFile.getPushNumber() + ids.size());
                } else {
                    updateFile.setErrorActualNumber(localFile.getErrorActualNumber() + ids.size());
                }
                localFileMapper.updateByPrimaryKeySelective(updateFile);
            }
            if (code) {
                updateEntity.setmStatus(3);
            } else {
                updateEntity.setmStatus(4);
            }
            PhoneSaleTransferExample transferExample = new PhoneSaleTransferExample();
            transferExample.createCriteria().andIdIn(ids);
            phoneSaleTransferMapper.updateByExampleSelective(updateEntity, transferExample);
            if (code) {
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            } else {
                return new Result().setCode(ResultCode.FAIL.getValue());
            }

        }
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }


    /**
     * 推送决策接口
     *
     * @param dto
     * @param retry
     * @return
     */
    @RetryMethod(retryNowNum = 2, isOrNoDbRetry = true)
    public Result callPolicyData(PolicyRetryByRuleDTO dto, Integer retry) {
        List<Long> ids = dto.getIds();
        PushMarketingUserDTO pushMarketingUserDTO = dto.getPushMarketingUserDTO();
        //重试
        try {
            if (ObjectUtils.equals(retry,1)) {
                JSONObject jsonObject = (JSONObject) dto.getPushMarketingUserDTO().getJsonData();
                PushMarketingUserTaskInfoDTO taskInfoDTO = JSONObject.toJavaObject(jsonObject, PushMarketingUserTaskInfoDTO.class);
                pushMarketingUserDTO.setJsonData(taskInfoDTO);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), "决策重试接口类型转化失败!"), e);
        }
        Long infoId = dto.getInfoId();
        Result result = intelligentCustomerServiceClient.pushUser(pushMarketingUserDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            if (infoId != null) {
                saveBizLog(Joiner.on(",").join(ids), InterfaceHandlerEnum.INIT_TO_POLICY.getCode(), infoId);
            }
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }

        String msg = "apiCode：" + pushMarketingUserDTO.getApiCode()
                + ",infoId：" + infoId + ",调用推送决策接口失败 -- " + JSON.toJSONString(result);
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), msg));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }
    /**
     * 推送决策接口（不进行db重试）
     *
     * @param dto
     * @param retry
     * @return
     */
    @RetryMethod(retryNowNum = 2, isOrNoDbRetry = false)
    public Result callPolicyDataNoDb(PolicyRetryByRuleDTO dto, Integer retry, String apiCode) {
        List<Long> ids = dto.getIds();
        PushMarketingUserDTO pushMarketingUserDTO = dto.getPushMarketingUserDTO();
        //重试
        try {
            if (ObjectUtils.equals(retry,1)) {
                JSONObject jsonObject = (JSONObject) dto.getPushMarketingUserDTO().getJsonData();
                PushMarketingUserTaskInfoDTO taskInfoDTO = JSONObject.toJavaObject(jsonObject, PushMarketingUserTaskInfoDTO.class);
                pushMarketingUserDTO.setJsonData(taskInfoDTO);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                    "apiCode" + apiCode + "决策重试接口类型转化失败!"), e);
        }
        Long infoId = dto.getInfoId();
        Result result = intelligentCustomerServiceClient.pushUser(pushMarketingUserDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            if (infoId != null) {
                saveBizLog(Joiner.on(",").join(ids), InterfaceHandlerEnum.INIT_TO_POLICY.getCode(), infoId);
            }
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }

        String msg = "apiCode：" + apiCode
                + ",infoId：" + infoId + ",调用推送决策接口失败 -- " + JSON.toJSONString(result);
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),msg));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    /**
     * 宜信情况L调用推送决策接口
     *
     * @param dto
     * @param retry
     * @return
     */
    @RetryMethod(retryNowNum = 2, isOrNoDbRetry = true)
    public Result callPolicyDataYiXinToJueCe(PolicyRetryByRuleDTO dto, Integer retry) {
        List<Long> ids = dto.getIds();
        PushMarketingUserDTO pushMarketingUserDTO = dto.getPushMarketingUserDTO();
        //重试
        try {
            if (ObjectUtils.equals(retry,1)) {
                JSONObject jsonObject = (JSONObject) dto.getPushMarketingUserDTO().getJsonData();
                PushMarketingUserTaskInfoDTO taskInfoDTO = JSONObject.toJavaObject(jsonObject, PushMarketingUserTaskInfoDTO.class);
                pushMarketingUserDTO.setJsonData(taskInfoDTO);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), "决策重试接口类型转化失败!"), e);
        }
        Result result = intelligentCustomerServiceClient.pushUser(pushMarketingUserDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            DataCompare dataCompare = new DataCompare(Joiner.on(",").join(ids), InterfaceHandlerEnum.INIT_TO_POLICY.getCode(), null);
            dataCompare.setRemark("yixinToJueCeL");
            dataCompareMapper.insertSelective(dataCompare);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                "调用推送决策接口失败--" + JSON.toJSONString(result)));
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

        // 众安明细推送Mock挡板，1：开启，0：关闭
        String zhongAnPushMock = marketingCommonConfig.getZhongAnPushMock();
        JSONObject jo = JSONObject.parseObject(zhongAnPushMock);
        if("1".equals(jo.getString("pushSwitch"))){
            log.warn("【众安锁定名单推送】"+"挡板开启, {}", JSONObject.toJSONString(jo));
            updatePushStatus(bo, 2, null);
            result.setCode(ResultCode.SUCCESS.getValue());
            return result;
        }

        Result<?> zhongAnResult = zhongAnClient.pushDetail(bo.getDataDTO());
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


    /**
     * 调用电销Ibu批量接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     *
     * @param datumList
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result callDassIbuBatchData(ArrayList<IbuReqDTO.Datum> datumList, Integer retry) {
        //重试方法 这里反序列化过来是JsonObject
        if (!(datumList.get(0) instanceof IbuReqDTO.Datum)) {
            ArrayList<IbuReqDTO.Datum> list = new ArrayList<>();
            for (int i = 0; i < datumList.size(); i++) {
                if (datumList.get(i) != null) {
                    list.add(JSON.parseObject(JSON.toJSONString(datumList.get(i)), IbuReqDTO.Datum.class));
                }
            }
            datumList = list;
        }
        Result result = dassServiceClient.pushIbuArtificial(datumList);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            Set<String> set = datumList.stream().map(IbuReqDTO.Datum::getId).map(String::valueOf).collect(Collectors.toSet());
            saveBizLog(String.join(",", set), InterfaceHandlerEnum.ARTIFICIAL_IBU_BATCH_DATA.getCode(),
                    null);
            phoneSaleExtendInfoMapper.updateBatch(set);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                , "调用人工IBU批量接口失败  --  " +  JSON.toJSONString(result)));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    @RetryMethod(isOrNoDbRetry = true)
    public Result dassIbuWithFile(ArrayList<IbuReqDTO.Datum> datumList, Integer retry) {
        try {
            //重试方法 这里反序列化过来是JsonObject
            if (!(datumList.get(0) instanceof IbuReqDTO.Datum)) {
                ArrayList<IbuReqDTO.Datum> list = new ArrayList<>();
                for (int i = 0; i < datumList.size(); i++) {
                    if (datumList.get(i) != null) {
                        list.add(JSON.parseObject(JSON.toJSONString(datumList.get(i)), IbuReqDTO.Datum.class));
                    }
                }
                datumList = list;
            }
            List<Long> ids = datumList.stream().map(t -> t.getId()).collect(Collectors.toList());
            PhoneSaleIbu updateEntity = new PhoneSaleIbu();
            LocalFile updateFile = new LocalFile();
            Result result = dassServiceClient.pushIbuArtificial(datumList);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                Set<String> set = datumList.stream().map(IbuReqDTO.Datum::getId).map(String::valueOf).collect(Collectors.toSet());
                if (new Integer(1).equals(retry)) {
                    Long id = ids.get(0);
                    PhoneSaleIbu phoneSaleIbu = phoneSaleIbuMapper.selectByPrimaryKey(id);
                    LocalFile localFile = localFileMapper.selectByPrimaryKey(Long.valueOf(phoneSaleIbu.getLocalId()));
                    updateFile.setId(localFile.getId());
                    updateFile.setPushNumber(localFile.getPushNumber() + ids.size());
                    localFileMapper.updateByPrimaryKeySelective(updateFile);
                }
                updateEntity.setmStatus(3);
                PhoneSaleIbuExample ibuExample = new PhoneSaleIbuExample();
                ibuExample.createCriteria().andIdIn(ids);
                phoneSaleIbuMapper.updateByExampleSelective(updateEntity, ibuExample);
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            } else if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            } else {
                updateEntity.setmStatus(4);
                PhoneSaleIbuExample ibuExample = new PhoneSaleIbuExample();
                ibuExample.createCriteria().andIdIn(ids);
                phoneSaleIbuMapper.updateByExampleSelective(updateEntity, ibuExample);
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                        , "调用人工IBU批量接口失败  --  " +  JSON.toJSONString(result)));
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), ex.getMessage()), ex);
        }

        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    private void updatePushStatus(ZaMarketDataBO bo, Integer updatePushStatus, Integer updateStatus) {
        updateCallPushStatus(bo, updatePushStatus, updateStatus);
        updateSmsPushStatus(bo, updatePushStatus, updateStatus);
    }

    private void updateCallPushStatus(ZaMarketDataBO bo, Integer updatePushStatus, Integer updateStatus) {
        if (CollectionUtils.isEmpty(bo.getIds())) {
            return;
        }
        ZhonganRosterLockingData data = new ZhonganRosterLockingData();
        if (updateStatus != null) {
            data.setStatus(updateStatus);
        }
        data.setPushStatus(updatePushStatus);
        data.setUpdateTime(new Date());
        ZhonganRosterLockingDataExample example = new ZhonganRosterLockingDataExample();
        example.createCriteria().andIdIn(bo.getIds());
        zhonganRosterLockingDataMapper.updateByExampleSelective(data, example);
    }


    private void updateSmsPushStatus(ZaMarketDataBO bo, Integer updatePushStatus, Integer updateStatus) {
        if (CollectionUtils.isEmpty(bo.getSmsIds())) {
            return;
        }
        ZhongAnSmsRosterLockingData data = new ZhongAnSmsRosterLockingData();
        if (updateStatus != null) {
            data.setStatus(updateStatus);
        }
        data.setPushStatus(updatePushStatus);
        data.setUpdateTime(new Date());
        ZhongAnSmsRosterLockingDataExample example = new ZhongAnSmsRosterLockingDataExample();
        example.createCriteria().andIdIn(bo.getSmsIds());
        zhongAnSmsRosterLockingDataMapper.updateByExampleSelective(data, example);
    }

    /**
     * 调用决策接口去重方法
     *
     * @return
     */
    @RetryMethod(retryNowNum = 2, isOrNoDbRetry = true)
    @DistributeLog
    public Result callPolicySoleData(PolicyRetryByRuleSoleDTO soleDTO, Integer o) {
        if (soleDTO.getData().size() <= 0) {
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }
        List<Long> ids = soleDTO.getIds();
        PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
        taskInfoDTO.setData(soleDTO.getData());
        taskInfoDTO.setAccessNumber(UUID.randomUUID().toString());
        taskInfoDTO.setMethod("caseAdd");
        taskInfoDTO.setBatchNumber(soleDTO.getBatchNumber());
        taskInfoDTO.setStrategyCode(soleDTO.getStrategyCode());

        PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
        if (StringUtils.isNotEmpty(marketingCommonConfig.getApiCodeMatch().get(soleDTO.getApiCode()))) {
            pushMarketingUserDTO.setApiCode(marketingCommonConfig.getApiCodeMatch().get(soleDTO.getApiCode()));
        } else {
            pushMarketingUserDTO.setApiCode(soleDTO.getApiCode());
        }
        pushMarketingUserDTO.setJsonData(taskInfoDTO);
        Result result = intelligentCustomerServiceClient.pushUser(pushMarketingUserDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            if (ids != null && ids.size() > 0) {
                saveBizLog(Joiner.on(",").join(ids), InterfaceHandlerEnum.INIT_TO_POLICY_SOLE.getCode(), soleDTO.getInfoId());
            }
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                "调用推送决策接口失败 -- " + JSON.toJSONString(result)));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    /**
     * 滴滴通话明细推送
     *
     * @param didiCallRecord
     * @param retry
     * @return
     */
    @RetryMethod(isOrNoDbRetry = true)
    public Result<Boolean> didiPushData(DidiCallRecord didiCallRecord, Integer retry) {

        Boolean res = Boolean.FALSE;
        try {
            String custNum = didiCallRecord.getCustNum();
            String apiCode = didiCallRecord.getApiCode();
            String meidaName = "bairongA";
            Integer createDate = didiCallRecord.getCreateDate();
            // 获取redis 锁
            String key = RedisKeyConstant.pushDidiCollRecordLock.concat(":")
                    .concat(apiCode)
                    .concat(custNum);
            String value = UUID.randomUUID().toString();

            redisChgService.lock(key, value);
            DidiCallRecord updateDidiCallRecord = new DidiCallRecord();
            updateDidiCallRecord.setId(didiCallRecord.getId());
            //查询当天是否推送过
            DidiCallRecordExample didiCallRecordExample = new DidiCallRecordExample();
            didiCallRecordExample.createCriteria()
                    .andCustNumEqualTo(custNum)
                    .andStatusEqualTo(3)
                    .andCreateDateEqualTo(createDate);
            if (didiCallRecordMapper.countByExample(didiCallRecordExample) == 0) {
                MarketingTransferSyncUser marketingTransferSyncUser = new MarketingTransferSyncUser();
                marketingTransferSyncUser.setApiCode(apiCode);
                marketingTransferSyncUser.setRequestData(LocalDate.now().toString());
                marketingTransferSyncUser.setCustNum(custNum);
                // 判断是否有效
                Map<String, SyncUserValidityPeriodsBO> validityPeriodsBOMap = transferDataValidityPeriodService
                        .getValidityPeriodsByCustNum(Collections.singleton(custNum), apiCode, null);
                SyncUserValidityPeriodsBO bo = validityPeriodsBOMap.get(custNum);
                if (bo != null) {
                    List<MarketingSyncUser> syncUsers = bo.getSyncUsers();
                    updateDidiCallRecord.setCell(syncUsers.get(0).getCell());
                    // 调接口推送
                    DiDiReqVO diDiReqVO = new DiDiReqVO();
                    diDiReqVO.setMediaName(meidaName);
                    diDiReqVO.setCustMobileMd5(custNum);
                    boolean isNotError;
                    Result<DiDiResponseTO> resResultResult;
                    DiDiReachBO diDiReachBO = new DiDiReachBO();
                    DiDiReachRequestTO diDiReachRequestTO = new DiDiReachRequestTO();
                    diDiReachRequestTO.setScas(didiCallRecord.getScas());
                    diDiReachBO.setDiDiReachRequestTO(diDiReachRequestTO);
                    diDiReachBO.setDiDiReqVO(diDiReqVO);
                    resResultResult = diDiClient.pushReachSuccess(diDiReachBO);
                    isNotError = resResultResult.getData() != null
                            && "10000".equals(resResultResult.getData().getErrorCode());
                    // 500 异常需要进入阶梯重试
                    if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(resResultResult.getCode())) {
                        updateDidiCallRecord.setStatus(2);
                        updateDidiCallRecord.setSysMessage("重试数据");
                        updateDidiCallRecord.setUpdateTime(new Date());
                        didiCallRecordMapper.updateByPrimaryKeySelective(updateDidiCallRecord);
                        // 解锁
                        redisChgService.unlock(key, value);
                        return new Result<Boolean>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                    }
                    // 成功则更新数据状态
                    if (resResultResult.getCode().equals(ResultCode.SUCCESS.getValue()) && isNotError) {
                        res = Boolean.TRUE;
                        DiDiResponseTO diDiResponseTO = resResultResult.getData();
                        DiDiResponseTO.ResResult data = diDiResponseTO.getData();
                        Boolean result = null;
                        if (data != null) {
                            result = data.getResult();
                        }
                        String errorMessage = diDiResponseTO.getErrorMessage();
                        String errorCode = diDiResponseTO.getErrorCode();
                        updateDidiCallRecord.setStatus(3);
                        updateDidiCallRecord.setResult(result);
                        updateDidiCallRecord.setErrorCode(errorCode);
                        updateDidiCallRecord.setErrorMessage(errorMessage);
                    } else {
                        updateDidiCallRecord.setStatus(2);
                        updateDidiCallRecord.setSysMessage("非500异常");
                    }

                } else {
                    updateDidiCallRecord.setStatus(2);
                    updateDidiCallRecord.setSysMessage("数据失效");
                }
            } else {
                updateDidiCallRecord.setStatus(2);
                updateDidiCallRecord.setSysMessage("数据重复");
            }
            // 处理返回结果
            updateDidiCallRecord.setUpdateTime(new Date());
            didiCallRecordMapper.updateByPrimaryKeySelective(updateDidiCallRecord);
            // 解锁
            redisChgService.unlock(key, value);
        } catch (Exception e) {
            log.error("滴滴接口推送异常", e);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
    }

    public Result<Boolean> didiAllow(DiDiAllowReqDTO dto, Integer retry) {
        DiDiReqVO diDiReqVO = new DiDiReqVO();
        diDiReqVO.setCustMobileMd5(dto.getMobile());
        Result<DiDiResponseTO> diDiResponseTOResult = diDiClient.pushSmsTrafficAccess(diDiReqVO);
        DidiData updateEntity = new DidiData();
        updateEntity.setId(dto.getId());
        updateEntity.setPushStatus(2);
        if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(diDiResponseTOResult.getCode())) {
            updateEntity.setPushStatus(3);
            didiDataMapper.updateByPrimaryKeySelective(updateEntity);
            return new Result<Boolean>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        DiDiResponseTO data = diDiResponseTOResult.getData();
        Boolean res = Boolean.FALSE;
        if (data.getData() != null && data.getData().getResult()) {
            res = Boolean.TRUE;
            updateEntity.setPushDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            updateEntity.setIsMarketing(DiDiAllowMarketingEnum.YES.getValue());
            didiDataMapper.updateByPrimaryKeySelective(updateEntity);
        } else if (data.getData() != null && !data.getData().getResult()) {
            updateEntity.setIsMarketing(DiDiAllowMarketingEnum.NO.getValue());
            didiDataMapper.updateByPrimaryKeySelective(updateEntity);
        } else {
            updateEntity.setIsMarketing(DiDiAllowMarketingEnum.NOKNOW.getValue());
            updateEntity.setDataMessage(data == null ? "" : JSON.toJSONString(data));
            didiDataMapper.updateByPrimaryKeySelective(updateEntity);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
    }

    /**
     * 保存触达删除记录接口
     *
     * @param reqBO 封装的数据
     * @param retry 重试切面使用的标记，正常业务调用时赋值null
     * @return 接口响应业务字段
     */
    @RetryMethod(retryNowNum = 1, isOrNoDbRetry = true)
    public Result<SaveReachDeleteRecordResp> callSaveReachDeleteRecord(SaveReachDeleteRecordReqBO reqBO, Integer retry) {
        Result<SaveReachDeleteRecordResp> result = new Result<>();
        SaveReachDeleteRecordReq req = reqBO.getReq();
        Result<ResponseData<SaveReachDeleteRecordResp>> dataResult = qiFuClients.sendSaveReachDeleteRecordData(req);
        result.setCode(dataResult.getCode());
        if (retry == null && reqBO.getLogId() == null) {
            result.setDate(insertSaveReachDeleteRecordLog(reqBO, dataResult));
        } else if (ResultCode.SUCCESS.getValue().equals(dataResult.getCode())) {
            ResponseData<SaveReachDeleteRecordResp> data = dataResult.getData();
            QifuSaveReachDeleteRecordApiPushLog updateLog = new QifuSaveReachDeleteRecordApiPushLog();
            updateLog.setId(reqBO.getLogId());
            // 重试后正常 3
            updateLog.setStatus(3);
            updateLog.setRespFlag(data.getFlag().toString());
            updateLog.setRespCode(data.getCode());
            updateLog.setRespMsg(data.getMsg());
            SaveReachDeleteRecordResp t = data.getData().getT();
            if (Objects.nonNull(t)) {
                updateLog.setQifuIsSucceed(t.getIsSucceed().toString());
                updateLog.setQifuMessage(t.getMessage());
            }
            qifuSaveReachDeleteRecordApiPushLogMapper.updateByPrimaryKeySelective(updateLog);
        }
        return result;
    }

    private SaveReachDeleteRecordResp insertSaveReachDeleteRecordLog(SaveReachDeleteRecordReqBO reqBO
            , Result<ResponseData<SaveReachDeleteRecordResp>> dataResult) {
        SaveReachDeleteRecordResp resp = null;
        SaveReachDeleteRecordReq req = reqBO.getReq();
        QifuSaveReachDeleteRecordApiPushLog pushLog = new QifuSaveReachDeleteRecordApiPushLog();
        pushLog.setBatchNo(req.getBatchNo());
        pushLog.setRequestNo(req.getRequestNo());
        pushLog.setApiCode(reqBO.getApiCode());
        pushLog.setSyncAppletDate(reqBO.getAppletDate());
        pushLog.setPushDate(LocalDate.now().toString());
        pushLog.setUpdateTime(new Date());
        pushLog.setCreateTime(pushLog.getUpdateTime());
        ResponseData<SaveReachDeleteRecordResp> data = dataResult.getData();
        if (ResultCode.SUCCESS.getValue().equals(dataResult.getCode())) {
            pushLog.setRespCode(data.getCode());
            pushLog.setRespFlag(data.getFlag().toString());
            pushLog.setRespMsg(data.getMsg());
            SaveReachDeleteRecordResp t = data.getData().getT();
            pushLog.setQifuIsSucceed(t.getIsSucceed().toString());
            pushLog.setQifuMessage(t.getMessage());
            resp = t;
            // 正常 1
            pushLog.setStatus(1);
        } else {
            if (Objects.nonNull(data)) {
                pushLog.setRespCode(data.getCode());
                pushLog.setRespFlag(data.getFlag().toString());
                pushLog.setRespMsg(data.getMsg());
            }
            pushLog.setErrorMsg(dataResult.getMessage());
            // 异常 2
            pushLog.setStatus(2);
        }
        qifuSaveReachDeleteRecordApiPushLogMapper.insertSelective(pushLog);
        reqBO.setLogId(pushLog.getId());
        return resp;
    }

    /**
     * 保存触达删除记录接口
     *
     * @param reqBO 封装的数据
     * @param retry 重试切面使用的标记，正常业务调用时赋值null
     * @return 接口响应业务字段
     */
    @RetryMethod(retryNowNum = 1, isOrNoDbRetry = true)
    public Result<SaveReachDeleteRecordResp> callDeleteReachRecordCuDongZhi(SaveReachDeleteRecordReqBO reqBO, Integer retry) {
        Result<SaveReachDeleteRecordResp> result = new Result<>();
        SaveReachDeleteRecordReq req = reqBO.getReq();
        Result<ResponseData<SaveReachDeleteRecordResp>> dataResult = qiFuClients.sendDeleteReachRecordDataCuDongZhi(req);
        result.setCode(dataResult.getCode());
        if (retry == null && reqBO.getLogId() == null) {
            result.setDate(insertSaveReachDeleteRecordLog(reqBO, dataResult));
        } else if (ResultCode.SUCCESS.getValue().equals(dataResult.getCode())) {
            ResponseData<SaveReachDeleteRecordResp> data = dataResult.getData();
            QifuSaveReachDeleteRecordApiPushLog updateLog = new QifuSaveReachDeleteRecordApiPushLog();
            updateLog.setId(reqBO.getLogId());
            // 重试后正常 3
            updateLog.setStatus(3);
            updateLog.setRespFlag(data.getFlag().toString());
            updateLog.setRespCode(data.getCode());
            updateLog.setRespMsg(data.getMsg());
            SaveReachDeleteRecordResp t = data.getData().getT();
            if (Objects.nonNull(t)) {
                updateLog.setQifuIsSucceed(t.getIsSucceed().toString());
                updateLog.setQifuMessage(t.getMessage());
            }
            qifuSaveReachDeleteRecordApiPushLogMapper.updateByPrimaryKeySelective(updateLog);
        }
        return result;
    }

    /**
     * 众邦财富推送标签评级
     *
     * @param json  封装的数据
     * @param retry 重试切面使用的标记，正常业务调用时赋值null
     * @return 接口响应业务字段
     */
    @RetryMethod(retryNowNum = 1, isOrNoDbRetry = true)
    public Result<ZbankResponse<ZbankLabelRatingReResultDTO>> pushZbankLabelRatingRe(JSONObject json
            , Integer retry) {
        //测试mock
        if(marketingCommonConfig.getZhongBangCaifuLabelTest()){
            log.warn("测试众邦不真实调用接口");
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }
        Result<ZbankResponse<ZbankLabelRatingReResultDTO>> result = new Result<>();
        JSONObject jsonData = new JSONObject();
        jsonData.putAll(json);
        jsonData.remove("ids");
        JSONObject object = new JSONObject();
        String requestId = "" + System.nanoTime() + RandomStringUtils.randomNumeric(5);
        if (retry == null) {
            jsonData.put("TxnSrlNo", requestId);
            jsonData.put("TxnDt", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
            jsonData.put("TxnTs", LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss[SSS]")));
        } else {
            jsonData.put("TxnSrlNo", "r" + requestId);
        }
        object.put("request", jsonData);
        String jsonStr;
        try {
            jsonStr = zBankClient.labelRatingRe(object, requestId);
        } catch (Exception e) {
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            log.error(e.getMessage(), e);
            return result;
        }
        ZbankResponse<ZbankLabelRatingReResultDTO> dto;
        try {
            dto = JSONObject.parseObject(jsonStr
                    , new TypeReference<ZbankResponse<ZbankLabelRatingReResultDTO>>() {
                    });
        } catch (Exception e) {
            log.error(e.getMessage() + "响应：" + jsonStr, e);
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }
        if ("000000".equals(dto.getCode())) {
            ZbankLabelRatingReResultDTO result1 = dto.getResult();
            if ("00".equals(result1.getErrCd())) {
                //重试成功后更新状态
                if (retry != null) {
                    JSONArray ids = json.getJSONArray("ids");
                    pushRuleService.updateZhongBangRetryStatus(ids.toJavaList(Long.class));
                }
                result.setCode(ResultCode.SUCCESS.getValue());
            } else if ("500".equals(result1.getErrCd())) {
                log.error("众邦财富推送标签评级接口实体不能为空或者回传数据更新失败,进入重试,响应：{},请求：{}"
                        , jsonStr, jsonData.toJSONString());
                result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            } else {
                result.setCode(ResultCode.FAIL.getValue());
                log.error("众邦财富推送标签评级接口未知错误,不会重试,响应：{}", jsonStr);
            }
        } else {
            log.error("众邦财富推送标签评级接口异常,进入重试,响应：{},请求：{}", jsonStr, jsonData.toJSONString());
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        result.setDate(dto);
        return result;
    }

    /**
     * 众邦录音明细回调
     *
     * @param json  封装的数据
     * @param retry 重试切面使用的标记，正常业务调用时赋值null
     * @return 接口响应业务字段
     */
    @RetryMethod(retryNowNum = 2, isOrNoDbRetry = false)
    public Result<ZbankResponse<ZbankLabelRatingReResultDTO>> pushZbankRecodFileRe(JSONObject json
            , Integer retry) {
        //测试mock
        String zhongBangJson = marketingCommonConfig.getZhongBangRecodFileReTest();
        if(StringUtils.isNotBlank(zhongBangJson)){
            JSONObject jsonObject = JSON.parseObject(zhongBangJson);
            if("true".equals(jsonObject.getString("open"))){
                String code = jsonObject.getString("code");
                if ("500".equals(code)) {
                    log.warn("测试众邦[流控]不真实调用接口-{}",code);
                    return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                } else if("0".equals(code)){
                    log.warn("测试众邦[失败]不真实调用接口-{}",code);
                    return new Result<>().setCode(ResultCode.FAIL.getValue());
                } else {
                    log.warn("测试众邦[成功]不真实调用接口-{}",code);
                    return new Result<>().setCode(ResultCode.SUCCESS.getValue());
                }
            }
        }
        Result<ZbankResponse<ZbankLabelRatingReResultDTO>> result = new Result<>();
        JSONObject jsonData = new JSONObject();
        jsonData.putAll(json);
        jsonData.remove("ids");
        JSONObject object = new JSONObject();
        String requestId = channelId + System.nanoTime() + RandomStringUtils.randomNumeric(8);
        if (retry == null) {
            jsonData.put("TxnSrlNo", requestId);
            jsonData.put("TxnDt", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
            jsonData.put("TxnTs", LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss[SSS]")));
        } else {
            jsonData.put("TxnSrlNo", "r" + requestId);
        }
        object.put("request", jsonData);
        String jsonStr;
        try {
            jsonStr = zBankClient.recodFileRe(object, requestId);
        } catch (Exception e) {
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            log.error(e.getMessage(), e);
            return result;
        }
        ZbankResponse<ZbankLabelRatingReResultDTO> dto;
        try {
            dto = JSONObject.parseObject(jsonStr
                    , new TypeReference<ZbankResponse<ZbankLabelRatingReResultDTO>>() {
                    });
        } catch (Exception e) {
            log.error(e.getMessage() + "响应：" + jsonStr, e);
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }
        if ("000000".equals(dto.getCode())) {
            ZbankLabelRatingReResultDTO result1 = dto.getResult();
            if ("00".equals(result1.getErrCd())) {
                //重试成功后更新状态
//                if (retry != null) {
//                    JSONArray ids = json.getJSONArray("ids");
//                    // TODO: 2024-05-16  重试回调方法
//                    zhongBangVoiceFileDetailMapper.updateBatchByIds(ids.toJavaList(Long.class),2);
//                }
                result.setCode(ResultCode.SUCCESS.getValue());
            } else {
                result.setCode(ResultCode.FAIL.getValue());
                log.error("众邦录音明细回调接口未知错误,不会重试,响应：{}", jsonStr);
            }
        } else if ("OPENAPI-I-00019".equals(dto.getCode())) {
            // 流控 需要重试
            log.warn("众邦录音明细回调接口出现流控,进入重试,响应：{}", jsonStr);
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        } else {
            log.error("众邦录音明细回调接口异常,不重试,响应：{},请求：{}", jsonStr, jsonData.toJSONString());
            result.setCode(ResultCode.FAIL.getValue());
        }
        result.setDate(dto);
        return result;
    }

    /**
     * 用户数据回传接口
     *
     * @param userDataCallBackRequest 封装的数据
     * @param retry                   重试切面使用的标记，正常业务调用时赋值null
     * @return 接口响应业务字段
     */
    @RetryMethod(retryNowNum = 2, isOrNoDbRetry = true)
    public Result<GmCallBackResponse<Object>> sendUserDataCallBack(GmUserDataCallBackRequest userDataCallBackRequest
            , Integer retry) {
        if (StringUtils.isBlank(userDataCallBackRequest.getRequestId())) {
            userDataCallBackRequest.setRequestId("dc" + System.nanoTime() + ""
                    + RandomStringUtils.randomAlphanumeric(15));
        }
        return guoMeiClient.sendUserDataCallBack(userDataCallBackRequest, Object.class);
    }

    /**
     * 营销结果数据回传接口
     *
     * @param resultCallBackRequest 封装的数据
     * @param retry                 重试切面使用的标记，正常业务调用时赋值null
     * @return 接口响应业务字段
     */
    @RetryMethod(retryNowNum = 2, isOrNoDbRetry = true)
    public Result<GmCallBackResponse<Object>> sendMarketingResultCallBack(GmMarketingResultCallBackRequest resultCallBackRequest
            , Integer retry) {
        if (StringUtils.isBlank(resultCallBackRequest.getRequestId())) {
            resultCallBackRequest.setRequestId("rc" + System.nanoTime() + ""
                    + RandomStringUtils.randomAlphanumeric(15));
        }
        return guoMeiClient.sendMarketingResultCallBack(resultCallBackRequest, Object.class);
    }


    /**
     * 推送萨摩耶接口
     *
     * @param commReqDto  封装的数据
     * @param retry 是否重试
     */
    @RetryMethod(retryNowNum = 3)
    public Result<?> sendSmyBlackList(SmyCommReqDto commReqDto, Integer retry) {
        // 萨摩耶推送Mock挡板，1：开启，0：关闭
        Object mock = marketingCommonConfig.getSmyBlacklistConfig().get("mock");
        if("1".equals(mock)){
            log.warn("【萨摩耶黑名单传输】挡板开启, {}", mock);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        return smyClient.sendSmyBlackList(commReqDto);
    }

    @RetryMethod(retryNowNum = 3)
    @Mockable(mockName = MockConstants.TEST_QIFIUQUERY_RETURN)
    public Result<ResponseData<QryCallRealTimeResp>> qryCallRealTime(QryCallRealTimeReq qryCallRealTimeReq, Integer retry) {
        return qiFuClients.qryCallRealTimeUrl(qryCallRealTimeReq);
    }

    /**
     * 众邦AI定制化回调
     * @param json  封装的数据
     * @param retry 重试切面使用的标记，正常业务调用时赋值null
     * @return 接口响应业务字段
     */
    @RetryMethod(retryNowNum = 2, isOrNoDbRetry = false)
    public Result<ZbankResponse<ZbankAIVoiceFileDetailResultDTO>> pushZbankRecodAIFileRe(JSONObject json
            , Integer retry) {
        Result<ZbankResponse<ZbankAIVoiceFileDetailResultDTO>> result = new Result<>();
        JSONObject jsonData = new JSONObject();
        JSONObject object = new JSONObject();
        String requestId = channelId + System.nanoTime() + RandomStringUtils.randomNumeric(8);
        jsonData.put("TxnSrlNo", requestId);
        jsonData.put("TxnDt", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        jsonData.put("TxnTs", LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss[SSS]")));
        jsonData.putAll(json);
        object.put("request", jsonData);
        String jsonStr;
        try {
            jsonStr = zBankClient.recodAIFileRe(object, requestId);
        } catch (Exception e) {
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            log.error("众邦AI录音明细回调接口异常"+e.getMessage(), e);
            return result;
        }
        ZbankResponse<ZbankAIVoiceFileDetailResultDTO> dto;
        try {
            dto = JSONObject.parseObject(jsonStr
                    , new TypeReference<ZbankResponse<ZbankAIVoiceFileDetailResultDTO>>() {
                    });
        } catch (Exception e) {
            log.error(e.getMessage() + "响应：" + jsonStr, e);
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }
        if ("000000".equals(dto.getCode())) {
            ZbankAIVoiceFileDetailResultDTO result1 = dto.getResult();
            if ("000000".equals(result1.getRetCd())) {
                result.setCode(ResultCode.SUCCESS.getValue());
            } else {
                result.setCode(ResultCode.FAIL.getValue());
                log.error("众邦AI录音明细回调接口未知错误,不会重试,响应：{}", jsonStr);
            }
        } else if ("OPENAPI-I-00019".equals(dto.getCode())) {
            // 流控 需要重试
            log.warn("众邦AI录音明细回调接口出现流控,进入重试,响应：{}", jsonStr);
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        } else {
            log.error("众邦AI录音明细回调接口异常,不重试,响应：{},请求：{}", jsonStr, jsonData.toJSONString());
            result.setCode(ResultCode.FAIL.getValue());
        }
        result.setDate(dto);
        return result;
    }

}
