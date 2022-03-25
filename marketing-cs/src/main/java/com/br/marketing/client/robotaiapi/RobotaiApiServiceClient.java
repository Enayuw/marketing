package com.br.marketing.client.robotaiapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneParentDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneQueryDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.RepQueryBlackPhoneVO;
import com.br.marketing.client.robotaiapi.output.ReqBlackPhoneVO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.common.utils.net.InterfaceLog;
import com.br.marketing.common.utils.net.MomCommonUtil;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.mapper.InterfaceLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class RobotaiApiServiceClient {

    @Value("${api.robotAiApiService.robotOutboundUrl:00}")
    private String robotOutboundUrl;

    @Value("${api.customerService.apiCode:0}")
    private String customerServiceApiCode;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MomCommonUtil momCommonUtil;

    @Qualifier("logDbpool")
    @Autowired
    public ThreadPoolExecutor logDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;

    public TransferRobotOutboundVO<UnsuccessfulData> pushRobotai(TransferRobotOutboundDTO dto,String requestId){
        dto.getJsonData().setPlatApiCode(customerServiceApiCode);
        try{
            InterfaceLog interfaceLog = new InterfaceLog();
            interfaceLog.setApiCode(dto.getApiCode());
            interfaceLog.setSwiftNumber(requestId);
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate,momCommonUtil,logDbpool).setUrl(robotOutboundUrl)
                    .setInterfaceLog(interfaceLog)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            if(!Integer.valueOf(200).equals(transfer.getHttpCode())){
                throw new RuntimeException("客服中心：".concat(String.valueOf(transfer.getHttpCode())));
            }
            TransferRobotOutboundVO<UnsuccessfulData> result = JSON.parseObject(transfer.getResult()
                    ,new TypeReference<TransferRobotOutboundVO>(){}.getType());
            return result;
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
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
            return result;
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
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
     * @param blackPhoneQueryDTO
     * @return RepQueryBlackPhoneVO
     */
    public RepQueryBlackPhoneVO  queryBlackPhone(ReqBlackPhoneQueryDTO blackPhoneQueryDTO){
        try{
            InterfaceLog interfaceLog = new InterfaceLog();
            interfaceLog.setApiCode(blackPhoneQueryDTO.getReqBlackPhoneDTO().getApiCode());
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate,momCommonUtil,logDbpool).setUrl(robotOutboundUrl)
                    .setInterfaceLog(interfaceLog)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(blackPhoneQueryDTO.getBlackQueryDetailDTOList()).postTransferStr();
            if(!Integer.valueOf(200).equals(transfer.getHttpCode())){
                throw new RuntimeException("客服中心：".concat(String.valueOf(transfer.getHttpCode())));
            }
            RepQueryBlackPhoneVO result = JSON.parseObject(transfer.getResult()
                    ,new TypeReference<RepQueryBlackPhoneVO>(){}.getType());
            return result;
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
            RepQueryBlackPhoneVO result = new RepQueryBlackPhoneVO();
            result.setCode("9999");
            result.setMessage(ex.getMessage());
            return result;
        }
    }
}
