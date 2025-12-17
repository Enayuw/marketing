package com.br.marketing.client.robotaiapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.cloud.counter.BrCounter;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.net.ApiCaller;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.client.robotaiapi.input.*;
import com.br.marketing.client.robotaiapi.output.*;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RobotaiApiServiceClient {

    @Value("${api.robotAiApiService.robotOutboundUrl:00}")
    private String robotOutboundUrl;

    @Value("${api.customerService.apiCode:0}")
    private String customerServiceApiCode;

    @Autowired
    RestTemplate restTemplate;

    @Qualifier("logDbpool")
    @Autowired
    public ThreadPoolExecutor logDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;

    @Autowired
    AlarmApiClient alarmApiClient;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    TrackingService trackingService;

    public static final int RETRY_COUNT = 2;

    public TransferRobotOutboundVO<UnsuccessfulData> pushRobotai(TransferRobotOutboundDTO dto, String requestId) {
        dto.getJsonData().setPlatApiCode(customerServiceApiCode);
        try {
            ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate, interfaceLogMapper, logDbpool).setUrl(robotOutboundUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            if (!Integer.valueOf(200).equals(transfer.getHttpCode())) {
                throw new RuntimeException("客服中心：".concat(String.valueOf(transfer.getHttpCode())));
            }
            TransferRobotOutboundVO<UnsuccessfulData> result = JSON.parseObject(transfer.getResult()
                    , new TypeReference<TransferRobotOutboundVO>() {
                    }.getType());
            try {
                //调用数量监控
                BrCounter.count(PrometheusMonitorUtils.COUNT_ROBOTAI_TRANSFER_METRIC_NAME, dto.getApiCode(), "transferData-api",
                        dto.getJsonData().getConversionData().size());

                //region 埋点
                try {
                    trackingService.trackDetailedLog(
                            DataFlowDirection.OUT
                            , dto.getApiCode()
                            , "推送外呼转化"
                            , JSON.toJSONString(dto.getJsonData())
                            , Boolean.TRUE
                            , Long.valueOf(dto.getJsonData().getConversionData().size())
                            , TrackingContext.generateBatchId());
                } catch (Exception ex) {
                    log.warn(
                            AlertLog.buildWarnMessage(
                                    AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                    , ex.getMessage()
                                    , "埋点异常")
                            , ex);
                }
                //endregion
            } catch (Exception ex) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), "推送客服转化接口统计异常！"), ex);
            }
            return result;
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), ex.getMessage()), ex);
            TransferRobotOutboundVO<UnsuccessfulData> result = new TransferRobotOutboundVO();
            result.setCode("9999");
            result.setMessage(ex.getMessage());
            return result;
        }
    }

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public TransferRobotOutboundVO<TransferRobotDataVO> pushRobotai(TransferRobotOutboundDTO dto) {
        dto.getJsonData().setPlatApiCode(customerServiceApiCode);
        try {
            ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate, interfaceLogMapper, logDbpool)
                    .setUrl(robotOutboundUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            if (!Integer.valueOf(200).equals(transfer.getHttpCode())) {
                throw new RuntimeException("客服中心：".concat(String.valueOf(transfer.getHttpCode())));
            }
            try {
                //调用数量监控
                BrCounter.count(PrometheusMonitorUtils.COUNT_ROBOTAI_TRANSFER_METRIC_NAME, dto.getApiCode(), "transferData-api",
                        dto.getJsonData().getConversionData().size());
                //region 埋点
                try {
                    trackingService.trackDetailedLog(
                            DataFlowDirection.OUT
                            , dto.getApiCode()
                            , "推送外呼转化"
                            , JSON.toJSONString(dto.getJsonData())
                            , Boolean.TRUE
                            , Long.valueOf(dto.getJsonData().getConversionData().size())
                            , TrackingContext.generateBatchId());
                } catch (Exception ex) {
                    log.warn(
                            AlertLog.buildWarnMessage(
                                    AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                    , ex.getMessage()
                                    , "埋点异常")
                            , ex);
                }
                //endregion
            } catch (Exception ex) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), "推送客服转化接口统计异常！"), ex);
            }
            TransferRobotOutboundVO<TransferRobotDataVO> result = JSON.parseObject(transfer.getResult()
                    , new TypeReference<TransferRobotOutboundVO<TransferRobotDataVO>>() {
                    }.getType());
            return result;
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), ex.getMessage()), ex);
            TransferRobotOutboundVO<TransferRobotDataVO> result = new TransferRobotOutboundVO();
            result.setCode("9999");
            result.setMessage(ex.getMessage());
            return result;
        }
    }

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public ReqBlackPhoneVO pushBlack(ReqBlackPhoneParentDTO parentDTO) {
        ReqBlackPhoneDTO dto = parentDTO.getDto();
        com.br.marketing.entity.InterfaceLog interfaceLog = new com.br.marketing.entity.InterfaceLog();
        interfaceLog.setExtendInfo(null);
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setUrl(robotOutboundUrl);
        interfaceLog.setCreateTime(new Date());
        interfaceLog.setRequestParam(JSON.toJSONString(dto));
        interfaceLog.setExtendInfo(parentDTO.getExtendInfo());
        Long start = System.currentTimeMillis();
        try {
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate).setUrl(robotOutboundUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            Long end = System.currentTimeMillis();
            interfaceLog.setResult(JSON.toJSONString(transfer));
            interfaceLog.setHttpCode(transfer.getHttpCode());
            interfaceLog.setExpire(String.valueOf(end - start));
            if (!Integer.valueOf(200).equals(transfer.getHttpCode())) {
                throw new RuntimeException("推送黑名单：".concat(String.valueOf(transfer.getHttpCode())));
            }
            ReqBlackPhoneVO result = JSON.parseObject(transfer.getResult()
                    , new TypeReference<ReqBlackPhoneVO>() {
                    }.getType());
            logDbpool.submit(() -> {
                try {
                    interfaceLogMapper.insertSelective(interfaceLog);
                } catch (Exception ex) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), "调用转化接口插入接口日志异常！"), ex);
                }
            });
            try {
                //调用数量监控
                BrCounter.count(PrometheusMonitorUtils.COUNT_ROBOTAI_BLACK_METRIC_NAME, dto.getApiCode(), "blackData-api",
                        parentDTO.getBlackDetailDTOList().size());
                //region 埋点
                try {
                    trackingService.trackDetailedLog(
                            DataFlowDirection.OUT
                            , dto.getApiCode()
                            , "推送外呼黑名单"
                            , JSON.toJSONString(dto.getJsonData())
                            , Boolean.TRUE
                            , Long.valueOf(parentDTO.getBlackDetailDTOList().size())
                            , TrackingContext.generateBatchId());
                } catch (Exception ex) {
                    log.warn(
                            AlertLog.buildWarnMessage(
                                    AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                    , ex.getMessage()
                                    , "埋点异常")
                            , ex);
                }
                //endregion
            } catch (Exception ex) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), "推送客服黑名单接口统计异常！"), ex);
            }
            return result;
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), ex.getMessage()), ex);
            interfaceLog.setResult(ex.getMessage().length() > 450 ? ex.getMessage().substring(0, 450) : ex.getMessage());
            Long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
            logDbpool.submit(() -> {
                try {
                    interfaceLogMapper.insertSelective(interfaceLog);
                } catch (Exception e) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), "调用转化接口插入接口日志异常！"), ex);
                }
            });
            ReqBlackPhoneVO result = new ReqBlackPhoneVO();
            result.setCode("9999");
            result.setMessage(ex.getMessage());
            return result;
        }

    }


    /**
     * 黑名单查询接口-宜信
     *
     * @param blackPhoneQueryDTO
     * @return RepQueryBlackPhoneVO
     */
    @RetryMethod(retryNowNum = 3)
    public Result<Map<String, String>> queryBlackPhone(ReqBlackPhoneQueryDTO blackPhoneQueryDTO) {
        Result result = new Result();
        try {
            ReqBlackPhoneDTO reqBlackPhoneDTO = new ReqBlackPhoneDTO();
            BlackPhoneDTO<BlackQueryDetailDTO> blackPhoneDTO = new BlackPhoneDTO<>();
            blackPhoneDTO.setData(blackPhoneQueryDTO.getDetailBlackPhoneDTO());
            blackPhoneDTO.setMethod("queryBlackDataV2");
            blackPhoneDTO.setAccessNumber(blackPhoneQueryDTO.getApiCode() + UUID.randomUUID().toString());
            reqBlackPhoneDTO.setApiCode(blackPhoneQueryDTO.getApiCode());
            reqBlackPhoneDTO.setJsonData(JSON.toJSONString(blackPhoneDTO));
            ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate, interfaceLogMapper, logDbpool)
                    .setUrl(robotOutboundUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(reqBlackPhoneDTO).postTransferStr();
            if (!Integer.valueOf(200).equals(transfer.getHttpCode())) {
                throw new RuntimeException("客服中心：".concat(String.valueOf(transfer.getHttpCode())));
            }
            RepQueryBlackPhoneVO repQueryBlackPhoneVO = JSON.parseObject(transfer.getResult()
                    , new TypeReference<RepQueryBlackPhoneVO>() {
                    }.getType());
            if (!"00".equals(repQueryBlackPhoneVO.getCode())) {
                return result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            Map<String, String> mapData;
            if (repQueryBlackPhoneVO.getData() != null) {
                List<RepQueryBlackPhoneDetailVO.SuccessData> successDataList = repQueryBlackPhoneVO.getData().getSuccessData();
                if (!CollectionUtils.isEmpty(successDataList)) {
                    mapData = successDataList.stream().collect(Collectors.toMap(RepQueryBlackPhoneDetailVO.SuccessData::getDataId,
                            RepQueryBlackPhoneDetailVO.SuccessData::getBlackFlag));
                    return result.setCode(ResultCode.SUCCESS.getValue()).setDate(mapData);
                }
            }
            return result.setCode(ResultCode.FAIL.getValue());
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), ex.getMessage()), ex);
            return result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    /**
     * @param dto
     * @param method
     * @return com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO<com.br.marketing.client.robotaiapi.output.TransferRobotDataVO>
     * @description 外呼接口
     * @author hedongshuo
     * @date 2024/12/2 11:38
     **/
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public RobotOutboundVo pushRobotOutbound(RobotOutboundGeneralDTO dto, String method) {
        RobotOutboundVo result = new RobotOutboundVo();
        HashMap<String, Object> mock = marketingCommonConfig.getThirdPartnerApiMethodMock();
        if (!Objects.isNull(mock)) {
            if (mock.get("switch") == Boolean.TRUE) {
                result.setCode(mock.get("code").toString());
                return result;
            }
        }
        try {
            ThirdApiResultTransfer thirdApiResult = new ApiCallerUtil(restTemplate, interfaceLogMapper, logDbpool)
                    .setUrl(robotOutboundUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            if (!Integer.valueOf(200).equals(thirdApiResult.getHttpCode())) {
                throw new RuntimeException("客服中心-method：".concat(method).concat("，httpCode：")
                        .concat(String.valueOf(thirdApiResult.getHttpCode())));
            }
            result = JSON.parseObject(thirdApiResult.getResult()
                    , new TypeReference<RobotOutboundVo>() {
                    }.getType());
            return result;
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), ex.getMessage()), ex);
            result = new RobotOutboundVo();
            result.setCode("9999");
            result.setMessage(ex.getMessage());
            return result;
        }
    }

    /**
     * 黑名单查询接口-宜信
     *
     * @return RepQueryBlackPhoneVO
     */
    public TransferRobotOutboundVO getSmsBaseInfo(TransferRobotOutboundDTO dto){
        log.warn("getSmsBaseInfo robotOutboundUrl:{}",robotOutboundUrl);
        dto.getJsonData().setPlatApiCode(customerServiceApiCode);
        try {
            ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate, interfaceLogMapper, logDbpool).setUrl(robotOutboundUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            if (!Integer.valueOf(200).equals(transfer.getHttpCode())) {
                throw new RuntimeException("客服中心：".concat(String.valueOf(transfer.getHttpCode())));
            }
            return JSON.parseObject(transfer.getResult()
                    , new TypeReference<TransferRobotOutboundVO>() {
                    }.getType());
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), ex.getMessage()), ex);
            TransferRobotOutboundVO result = new TransferRobotOutboundVO();
            result.setCode("9999");
            result.setMessage(ex.getMessage());
            return result;
        }
    }


}
