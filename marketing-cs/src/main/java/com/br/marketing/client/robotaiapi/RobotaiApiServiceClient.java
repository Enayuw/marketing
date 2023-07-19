package com.br.marketing.client.robotaiapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.cloud.counter.BrCounter;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.client.robotaiapi.input.*;
import com.br.marketing.client.robotaiapi.output.*;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.mapper.InterfaceLogMapper;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    public static final int RETRY_COUNT=2;

    public TransferRobotOutboundVO<UnsuccessfulData> pushRobotai(TransferRobotOutboundDTO dto,String requestId){
        dto.getJsonData().setPlatApiCode(customerServiceApiCode);
        try{
            ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate,interfaceLogMapper,logDbpool).setUrl(robotOutboundUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            if(!Integer.valueOf(200).equals(transfer.getHttpCode())){
                throw new RuntimeException("客服中心：".concat(String.valueOf(transfer.getHttpCode())));
            }
            TransferRobotOutboundVO<UnsuccessfulData> result = JSON.parseObject(transfer.getResult()
                    ,new TypeReference<TransferRobotOutboundVO>(){}.getType());
            try {
                //调用数量监控
                BrCounter.count(PrometheusMonitorUtils.COUNT_ROBOTAI_TRANSFER_METRIC_NAME, dto.getApiCode(), "transferData-api",
                        dto.getJsonData().getConversionData().size());
            } catch (Exception ex) {
                log.error("推送客服转化接口统计异常" + ex.getMessage(), ex);
            }
            return result;
        }catch (Exception ex){
            log.warn(ex.getMessage(), ex);
            alarmApiClient.sendAlarm(ex.getMessage(), "", AlarmSendCodeEnum.ERROR_UNKNOWN.getCode());
            TransferRobotOutboundVO<UnsuccessfulData> result = new TransferRobotOutboundVO();
            result.setCode("9999");
            result.setMessage(ex.getMessage());
            return result;
        }
    }

    public TransferRobotOutboundVO<UnsuccessfulData> pushRobotai(TransferRobotOutboundDTO dto){
        dto.getJsonData().setPlatApiCode(customerServiceApiCode);
        try{
            ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate,interfaceLogMapper,logDbpool)
                    .setUrl(robotOutboundUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            if(!Integer.valueOf(200).equals(transfer.getHttpCode())){
                throw new RuntimeException("客服中心：".concat(String.valueOf(transfer.getHttpCode())));
            }
            TransferRobotOutboundVO<UnsuccessfulData> result = JSON.parseObject(transfer.getResult()
                    ,new TypeReference<TransferRobotOutboundVO>(){}.getType());
            try {
                //调用数量监控
                BrCounter.count(PrometheusMonitorUtils.COUNT_ROBOTAI_TRANSFER_METRIC_NAME, dto.getApiCode(), "transferData-api",
                        dto.getJsonData().getConversionData().size());
            } catch (Exception ex) {
                log.error("推送客服转化接口统计异常" + ex.getMessage(), ex);
            }
            return result;
        }catch (Exception ex){
            log.warn(ex.getMessage(), ex);
            alarmApiClient.sendAlarm(ex.getMessage(), "", AlarmSendCodeEnum.ERROR_UNKNOWN.getCode());
            TransferRobotOutboundVO<UnsuccessfulData> result = new TransferRobotOutboundVO();
            result.setCode("9999");
            result.setMessage(ex.getMessage());
            return result;
        }
    }

    public ReqBlackPhoneVO pushBlack(ReqBlackPhoneParentDTO parentDTO){
        ReqBlackPhoneDTO dto = parentDTO.getDto();
        com.br.marketing.entity.InterfaceLog interfaceLog = new com.br.marketing.entity.InterfaceLog();
        interfaceLog.setExtendInfo(null);
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setUrl(robotOutboundUrl);
        interfaceLog.setCreateTime(new Date());
        interfaceLog.setRequestParam(JSON.toJSONString(dto));
        interfaceLog.setExtendInfo(parentDTO.getExtendInfo());
        Long start = System.currentTimeMillis();
        try{
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate).setUrl(robotOutboundUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            Long end = System.currentTimeMillis();
            interfaceLog.setResult(JSON.toJSONString(transfer));
            interfaceLog.setHttpCode(transfer.getHttpCode());
            interfaceLog.setExpire(String.valueOf(end - start));
            if(!Integer.valueOf(200).equals(transfer.getHttpCode())){
                throw new RuntimeException("推送黑名单：".concat(String.valueOf(transfer.getHttpCode())));
            }
            ReqBlackPhoneVO result = JSON.parseObject(transfer.getResult()
                    ,new TypeReference<ReqBlackPhoneVO>(){}.getType());
            logDbpool.submit(() -> {
                try {
                    interfaceLogMapper.insertSelective(interfaceLog);
                } catch (Exception ex) {
                    log.error(String.format("调用转化接口插入接口日志报错:%s", ex.getMessage()), ex);
                }
            });
            try {
                //调用数量监控
                BrCounter.count(PrometheusMonitorUtils.COUNT_ROBOTAI_BLACK_METRIC_NAME, dto.getApiCode(), "blackData-api",
                        parentDTO.getBlackDetailDTOList().size());
            } catch (Exception ex) {
                log.error("推送客服黑名单接口统计异常" + ex.getMessage(), ex);
            }
            return result;
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
            interfaceLog.setResult(ex.getMessage().length()>450? ex.getMessage().substring(0,450) : ex.getMessage());
            Long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
            logDbpool.submit(() -> {
                try {
                    interfaceLogMapper.insertSelective(interfaceLog);
                } catch (Exception e) {
                    log.error(String.format("调用转化接口插入接口日志报错:%s", e.getMessage()), e);
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
    public Result<Map<String,String>> queryBlackPhone(ReqBlackPhoneQueryDTO blackPhoneQueryDTO) {
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
            log.error(ex.getMessage(), ex);
            return result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

}
