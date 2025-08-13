package com.br.marketing.datarelayservice.processor;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.tc.TcDataDto;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.dto.tc.TcResponseDTO;
import com.br.marketing.enums.TcCpaRecordStatusEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.tc.RSAUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;

@Slf4j
public abstract class AbstractTcCustomizeProcessor {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * @description 模板方法
     * @param tcRequestDTO
     * @param clazz
     * @return com.br.marketing.dto.tc.TcResponseDTO
     * @author hedongshuo
     * @date 2025/4/23 20:04
     **/
    public final <T extends TcDataDto> TcResponseDTO process(TcRequestDTO tcRequestDTO, String apiCode, Class<T> clazz, String bizCode) {
        log.warn("接收到同程易融{}请求数据，data:{}", bizCode, tcRequestDTO);
        TcResponseDTO resdto = new TcResponseDTO();
        Long recordId = null;
        JSONObject tcyrServerConfig = marketingCommonConfig.getTcyrServerConfig();
        String tcPublicKey = tcyrServerConfig.getString("tcPublicKey");
        String brPrivateKey = tcyrServerConfig.getString("brPrivateKey").replace("*", "=");
        try {
            //1.保存记录
            TcDataDto tcDataDto = objectMapper.readValue(tcRequestDTO.getData(), clazz);
            apiCode = StringUtils.isNotBlank(apiCode) ? apiCode : fetchApiCode();
            recordId = recordSave(tcRequestDTO, tcDataDto.getBatchNo(), apiCode, brPrivateKey);
            if(null == recordId){
                return resdto.idempotentFail(brPrivateKey);
            }
            //2.公共必输项校验
            if (StringUtils.isNotEmpty(tcRequestDTO.validate())) {
                updateRecord(recordId, TcCpaRecordStatusEnum.ACCESS_FAIL.getValue(), tcRequestDTO.validate());
                return resdto.outterParamsFail(brPrivateKey, tcRequestDTO.validate());
            }
            //3.验签
            if (!RSAUtil.SignVf(tcRequestDTO, tcPublicKey)) {
                updateRecord(recordId, TcCpaRecordStatusEnum.ACCESS_FAIL.getValue(), TcResponseDTO.ResultEnum.SIGN_ERROR.getMsg());
                return resdto.signFail(brPrivateKey);
            }
            //4.data层必填项校验
            if (StringUtils.isNotEmpty(tcDataDto.validate())) {
                updateRecord(recordId, TcCpaRecordStatusEnum.ACCESS_FAIL.getValue(), tcDataDto.validate());
                return resdto.innerParamsFail(brPrivateKey, tcDataDto.validate());
            }
            //5.将record更新为status = 1-接入成功
            updateRecord(recordId, TcCpaRecordStatusEnum.ACCESS_SUCCESS.getValue(), null);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "同程数据接入异常！"), e);
            if (null != recordId) {
                updateRecord(recordId, TcCpaRecordStatusEnum.ACCESS_FAIL.getValue(), e.getMessage());
            }
            return resdto.systemFail(brPrivateKey);
        }
        return resdto.success(brPrivateKey);
    }

    protected abstract String fetchApiCode();

    /**
     * @description record更新
     * @param recordId
     * @param status
     * @param msg
     * @return void
     * @author hedongshuo
     * @date 2025/4/23 18:09
     **/
    protected abstract void updateRecord(Long recordId, Integer status, String msg);

    /**
     * @description record保存
     * @param tcRequestDTO
     * @param batchNo
     * @param apiCode
     * @param brPrivateKey
     * @return java.lang.Long
     * @author hedongshuo
     * @date 2025/4/23 18:09
     **/
    protected abstract Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey);

    public String apiCode() {
        return marketingCommonConfig.getTcyrApiCode();
    }

    public String cpaApiCode() {
        return marketingCommonConfig.getTcyrCpaApiCode();
    }
}
