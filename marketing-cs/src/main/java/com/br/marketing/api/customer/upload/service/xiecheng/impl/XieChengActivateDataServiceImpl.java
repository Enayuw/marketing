package com.br.marketing.api.customer.upload.service.xiecheng.impl;

import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import com.br.common.encryption.Md5Utils;
import com.br.marketing.api.customer.upload.adapter.BaseUploadDataAdaptee;
import com.br.marketing.api.customer.upload.handler.CustomerUploadHandlerEnum;
import com.br.marketing.api.customer.upload.service.xiecheng.XieChengActivateDataService;
import com.br.marketing.api.customer.upload.service.xiecheng.dto.XieChengActivateDataResponseDTO;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.dto.MarketingPreUserDTO;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class XieChengActivateDataServiceImpl implements XieChengActivateDataService {
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

    /**
     * 2023-10-18 16:45 客户
     *
     * @return 客户枚举
     */
    @Override
    public CustomerUploadHandlerEnum customer() {
        return CustomerUploadHandlerEnum.B_XIECHENG_ACTIVATE;
    }

    /**
     * 2023-10-18 16:45 反序列化客户定制数据
     *
     * @param jsonData json 字符串
     * @return 转化适配者
     */
    @Override
    public BaseUploadDataAdaptee parseObject(String jsonData) {
        return new BaseUploadDataAdaptee<MarketingPreUserDTO>() {
            private static final long serialVersionUID = 8794287668420049112L;
            @Override
            protected MarketingPreUserDTO adapteeRequest(String apiCode, String jsonData) {
                return null;
            }
        };
    }

    /**
     * 2023-10-23 17:37 校验字段
     *
     * @param adaptee 客户定制数据
     * @return 封装了响应结果与标记客户数据的状况
     */
    @Override
    public CustomerResponseDTO verifyFields(BaseUploadDataAdaptee adaptee) {
        XieChengActivateDataResponseDTO activateDataResponseDTO = new XieChengActivateDataResponseDTO();
        activateDataResponseDTO.success();
        return new CustomerResponseDTO(activateDataResponseDTO, CustomerResponseDTO.StatusEnum.VALID, activateDataResponseDTO.getCode());
    }

    /**
     * 获取requestId
     *
     * @param apiCode
     * @param adaptee 适配器
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    @Override
    public String getRequestId(String apiCode, BaseUploadDataAdaptee adaptee) {
        return apiCode.concat("_br_").concat(Md5Utils.cell32(RandomStringUtils.randomAlphabetic(32).concat("&") + System.nanoTime()));

    }

    /**
     * 2023-10-23 17:37 获取业务数据量
     *
     * @param adaptee 客户定制数据
     * @return 传输的业务数据量
     */
    @Override
    public int countBizDataNumber(BaseUploadDataAdaptee adaptee) {
        return countBizDataNumber(adaptee.getJsonData());
    }

    /**
     * 2023-10-24 19:24 获取全部的业务字段,用于检查是否有新增的字段
     *
     * @param jsonData 客户json字符串
     * @return 业务中要提示的新增字段
     */
    @Override
    public Set<String> getBizAllFields(String jsonData) {
        return null;
    }

    /**
     * 2023-10-24 19:17 json解析错误,对应响应
     *
     * @param e 业务异常
     * @return 定制化客户响
     */
    @Override
    public CustomerResponseDTO jsonErrorResponse(Exception e) {
        XieChengActivateDataResponseDTO activateDataResponseDTO = new XieChengActivateDataResponseDTO();
        activateDataResponseDTO.failed(XieChengActivateDataResponseDTO.ResultEnum.FAILED_JSON_ERROR);
        return new CustomerResponseDTO(activateDataResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, activateDataResponseDTO.getCode());
    }

    /**
     * 2023-10-24 19:17 业务中发生异常时,对应响应
     *
     * @param e 业务异常
     * @return 定制化客户响
     */
    @Override
    public CustomerResponseDTO bizErrorResponse(Exception e) {
        return fallbackResponse(e);
    }

    /**
     * 2023-10-24 19:17 回退响应,未知异常时,提升客户体验
     *
     * @param e 未知异常
     * @return 定制化客户响
     */
    @Override
    public CustomerResponseDTO fallbackResponse(Exception e) {
        XieChengActivateDataResponseDTO activateDataResponseDTO = new XieChengActivateDataResponseDTO();
        activateDataResponseDTO.failed();
        return new CustomerResponseDTO(activateDataResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, activateDataResponseDTO.getCode());
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
        //TODO 数据下发
        log.warn("携程促活数据下发 tCid:{},sourceId:{}",tCid,sourceId);
    }
}
