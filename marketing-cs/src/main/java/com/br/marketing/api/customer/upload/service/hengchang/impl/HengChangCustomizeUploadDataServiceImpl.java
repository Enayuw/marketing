package com.br.marketing.api.customer.upload.service.hengchang.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.upload.adapter.BaseUploadDataAdaptee;
import com.br.marketing.api.customer.upload.handler.CustomerUploadHandlerEnum;
import com.br.marketing.api.customer.upload.service.hengchang.HengChangCustomizeUploadDataService;
import com.br.marketing.api.customer.upload.service.hengchang.dto.HengChangUploadResponseDTO;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @ClassName HengChangCustomizeUploadDataServiceImpl
 * @Author kongbx
 * @Date 2025/1/3 15:23
 */
@Service
@Slf4j
public class HengChangCustomizeUploadDataServiceImpl implements HengChangCustomizeUploadDataService {

    @Resource
    private MarketingCustomerMapper marketingCustomerService;

    @Resource
    private RabbitMqProducter rabbitMqProducter;

    /**
     * 解密jsonData
     *
     * @param apiCode  apiCode
     * @param jsonData jsonData
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/09/11
     */
    @Override
    public String decryptJsonData(String apiCode, String jsonData) {
        return jsonData;
    }

    @Override
    public CustomerUploadHandlerEnum customer() {
        return CustomerUploadHandlerEnum.U_HENGCHANG;
    }

    /**
     * 2023-10-18 16:45 反序列化客户定制数据
     *
     * @param jsonData json 字符串
     * @return 转化适配者
     */
    @Override
    public BaseUploadDataAdaptee<MarketingPreUserDTO> parseObject(String jsonData) {
        return new BaseUploadDataAdaptee<MarketingPreUserDTO>() {
            private static final long serialVersionUID = 8794287668420049112L;
            @Override
            protected MarketingPreUserDTO adapteeRequest(String apiCode, String jsonData) {
                return null;
            }
        };
    }

    @Override
    public CustomerResponseDTO verifyFields(BaseUploadDataAdaptee adaptee) {
        HengChangUploadResponseDTO HengChangUploadResponseDTO = new HengChangUploadResponseDTO();
        HengChangUploadResponseDTO.success();
        List<MarketingCustomer> nameList = null;
        try {
            nameList = marketingCustomerService.getNameByApiCodeList(adaptee.getApiCode());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        String name;
        String shortName;
        if (CollectionUtils.isEmpty(nameList)) {
            name = customer().getName();
            shortName = name;
        } else {
            MarketingCustomer customer = nameList.get(0);
            name = customer.getName();
            shortName = customer.getShortName();
        }
        String msg = AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode(),
                "定制化上传接口接收到“" + shortName + "”编号“" + adaptee.getApiCode().concat("”的数据\n请及时与该“").concat(name).concat("”沟通确认^_^"), "通用定制化上传接口未知请求通知");
        log.warn(msg);
        return new CustomerResponseDTO(HengChangUploadResponseDTO, CustomerResponseDTO.StatusEnum.VALID, HengChangUploadResponseDTO.getCode());
    }

    /**
     * 获取requestId
     *
     * @param apiCode apiCode
     * @param adaptee 适配器
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    @Override
    public String getRequestId(String apiCode, BaseUploadDataAdaptee adaptee) {
        return apiCode.concat("_br_").concat(Md5Utils.cell32(RandomStringUtils.randomAlphabetic(32).concat("&") + System.nanoTime()));
    }

    @Override
    public int countBizDataNumber(BaseUploadDataAdaptee adaptee) {
        return countBizDataNumber(adaptee.getJsonData());
    }

    @Override
    public Set<String> getBizAllFields(String jsonStr) {
        return null;
    }

    @Override
    public CustomerResponseDTO jsonErrorResponse(Exception e) {
        HengChangUploadResponseDTO HengChangUploadResponseDTO = new HengChangUploadResponseDTO();
        HengChangUploadResponseDTO.failed(MarketingErrorInfo.JSON_DATA_ERROR);
        return new CustomerResponseDTO(HengChangUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, HengChangUploadResponseDTO.getCode());
    }

    @Override
    public CustomerResponseDTO bizErrorResponse(Exception e) {
        return fallbackResponse(e);
    }

    @Override
    public CustomerResponseDTO fallbackResponse(Exception e) {
        HengChangUploadResponseDTO HengChangUploadResponseDTO = new HengChangUploadResponseDTO();
        HengChangUploadResponseDTO.failed(MarketingErrorInfo.UNKNOWN_ERROR);
        return new CustomerResponseDTO(HengChangUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, HengChangUploadResponseDTO.getCode());
    }

    /**
     * 入库异常默认成功响应
     *
     * @return {@link CustomerResponseDTO }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    @Override
    public CustomerResponseDTO defaultSuccessResponse() {
        HengChangUploadResponseDTO HengChangUploadResponseDTO = new HengChangUploadResponseDTO();
        HengChangUploadResponseDTO.success();
        return new CustomerResponseDTO(HengChangUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, HengChangUploadResponseDTO.getCode());
    }

    /**
     * 数据下发
     *
     * @param tCid     tCid
     * @param sourceId 数据源主键id
     * @author senyang.zheng
     * @date 2024/09/25
     */
    @Override
    public void dataDirection(String tCid, Long sourceId) {
        try {
            JSONObject json = new JSONObject();
            json.put("tCid", tCid);
            json.put("sourceId", sourceId);
            rabbitMqProducter.send(MQConstants.ROUTING_KEY_MARKETING_HENGCHANG_DATA_CLEAN, json.toJSONString());
            log.warn("恒昌定制数据下发 tCid:{},sourceId:{}", tCid, sourceId);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HENGCHANG_SERVICEERROR.getCode(), e.getMessage()
                    , "推送恒昌定制数据下发消息异常！"), e);
        }
    }

}
