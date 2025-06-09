package com.br.marketing.client.marketingapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.PushTransferDataDTO;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.client.marketingapi.input.UploadDataUrlDTO;
import com.br.marketing.client.net.ApiCaller;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.entity.TwosevenFile;
import com.br.marketing.entity.TwosevenFileExample;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import com.br.marketing.mapper.TwosevenFileMapper;
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
public class MarketingApiService {
    
    @Autowired
    RestTemplate restTemplate;

    @Qualifier("interfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor interfaceLogDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;

    @Autowired
    TwosevenFileMapper twosevenFileMapper;

    @Value("${api.marketing.transferUrl:00}")
    String transferUrl;

    @Value("${api.marketing.uploadUrl:00}")
    String uploadUrl;

    /**
     * 奇富360促动支调用转化数据上传接口
     * @param dto 转化数据对应值
     * @param retry retry
     * @return Result<Boolean>
     */
    @RetryMethod(retryNowNum = 2)
    public Result<Boolean> pushMarketingApiTransfer(PushTransferDataDetailDTO dto, Integer retry) {
        try{
            ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate,interfaceLogMapper,interfaceLogDbpool)
                    .setUrl(transferUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto)
                    .setEncode(Boolean.TRUE)
                    .postTransferStr();
            if (Integer.valueOf(200).equals(transfer.getHttpCode())) {
                JSONObject jsonObject = JSON.parseObject(transfer.getResult());
                String code = jsonObject.getString("code");
                if ("00".equals(code)) {
                    return new Result().setCode(ResultCode.SUCCESS.getValue());
                }else{
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
                }
            }else{
                return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
        }catch (Exception ex){
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_APIERROR.getCode(), "调用营销转化数据上传接口报错!"), ex);
        }
        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
    }
    public Result<Boolean> pushTransfer(PushTransferDataDTO pushTransferDataDTO) {
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setExtendInfo(null);
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setUrl(transferUrl);
        interfaceLog.setCreateTime(new Date());
        interfaceLog.setRequestParam(JSON.toJSONString(pushTransferDataDTO.getDto()));
        interfaceLog.setExtendInfo(pushTransferDataDTO.getExtendInfo());
        Long start = System.currentTimeMillis();
        try{
        ThirdApiResultTransfer transfer = new ApiCaller(restTemplate)
                .setUrl(transferUrl)
                .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                .setRequestParam(pushTransferDataDTO.getDto())
                .postTransferStr();
        Long end = System.currentTimeMillis();
        interfaceLog.setResult(JSON.toJSONString(transfer));
        interfaceLog.setHttpCode(transfer.getHttpCode());
        interfaceLog.setExpire(String.valueOf(end - start));
        if (Integer.valueOf(200).equals(transfer.getHttpCode())) {
            interfaceLogDbpool.submit(() -> {
                try {
                    interfaceLogMapper.insertSelective(interfaceLog);
                } catch (Exception ex) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_APIERROR.getCode(), "调用转化接口插入接口日志报错!"), ex);
                }
            });
            JSONObject jsonObject = JSON.parseObject(transfer.getResult());
            String code = jsonObject.getString("code");
            if ("999999".equals(code)) {
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }
            if (!"00".equals(code)) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_APIERROR.getCode()
                        , String.format("推送转化接口错误：%s", transfer.getResult())));
                return new Result().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
            }
            TwosevenFileExample fileExample = new TwosevenFileExample();
            fileExample.createCriteria().andIdIn(pushTransferDataDTO.getTwoFileIds());
            TwosevenFile updateFile = new TwosevenFile();
            updateFile.setPushStatus(2);
            twosevenFileMapper.updateByExampleSelective(updateFile, fileExample);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        }catch (Exception ex){
            interfaceLog.setResult(ex.getMessage().length()>450? ex.getMessage().substring(0,450) : ex.getMessage());
            Long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
        }
        interfaceLogDbpool.submit(() -> {
            try {
                interfaceLogMapper.insertSelective(interfaceLog);
            } catch (Exception ee) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_APIERROR.getCode(), "调用转化接口插入接口日志报错!"), ee);
            }
        });

        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.TRUE);
    }

    public Result<Boolean> pushUpload(UploadDataDTO dto) {
        try{
            ThirdApiResultTransfer res = new ApiCallerUtil(restTemplate,interfaceLogMapper,interfaceLogDbpool)
                    .setUrl(uploadUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto)
                    .setEncode(Boolean.TRUE)
                    .postTransferStr();
            if (Integer.valueOf(200).equals(res.getHttpCode())) {
                JSONObject jsonObject = JSON.parseObject(res.getResult());
                String code = jsonObject.getString("code");
                if (!"00".equals(code)) {
                    return new Result().setCode(ResultCode.FAIL.getValue());
                }
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }else{
                return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
        }catch (Exception ex){
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_APIERROR.getCode(), "调用营销上传接口报错!"), ex);
            return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    @RetryMethod(retryNowNum = 2,isOrNoDbRetry = true)
    public Result callUploadDataByUrlRetry(UploadDataUrlDTO dto, Integer retry) {
        try{
            ThirdApiResultTransfer res = new ApiCallerUtil(restTemplate,interfaceLogMapper,interfaceLogDbpool)
                    .setUrl(dto.getUrl())
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto.getUploadDataDTO())
                    .postTransferStr();
            if (Integer.valueOf(200).equals(res.getHttpCode())) {
                JSONObject jsonObject = JSON.parseObject(res.getResult());
                String code = jsonObject.getString("code");
                if (!"00".equals(code)) {
                    return new Result().setCode(ResultCode.FAIL.getValue());
                }
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }else{
                return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
        }catch (Exception ex){
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_APIERROR.getCode(), "调用营销接口报错!"), ex);
            return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

}
