package com.br.marketing.api.customer.upload.service.guomei.impl;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.api.customer.upload.adapter.BaseUploadDataAdaptee;
import com.br.marketing.api.customer.upload.handler.CustomerUploadHandlerEnum;
import com.br.marketing.api.customer.upload.service.guomei.GuoMeiCustomizeUploadDataService;
import com.br.marketing.api.customer.upload.service.guomei.dto.GuMeUploadJsonDTO;
import com.br.marketing.api.customer.upload.service.guomei.dto.GuMeUploadResponseDTO;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.dto.CustomerResponseDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * 国美自定义上传策略实现
 *
 * @author senyang.zheng
 * @date 2024/08/07
 */
@Service
@Slf4j
public class GuoMeiCustomizeUploadDataServiceImpl implements GuoMeiCustomizeUploadDataService {
    /**
     * 2023-10-18 16:45 客户
     *
     * @return 客户枚举
     */
    @Override
    public CustomerUploadHandlerEnum customer() {
        return CustomerUploadHandlerEnum.U_GUME;
    }

    /**
     * 2023-10-18 16:45 反序列化客户定制数据
     *
     * @param jsonData json 字符串
     * @return 转化适配者
     */
    @Override
    public BaseUploadDataAdaptee parseObject(String jsonData) {
        return JSONObject.parseObject(jsonData, GuMeUploadJsonDTO.class);
    }

    /**
     * 2023-10-23 17:37 校验字段
     *
     * @param adaptee 客户定制数据
     * @return 封装了响应结果与标记客户数据的状况
     */
    @Override
    public CustomerResponseDTO verifyFields(BaseUploadDataAdaptee adaptee) {
        GuMeUploadJsonDTO uploadJsonDTO = (GuMeUploadJsonDTO)adaptee;
        GuMeUploadResponseDTO guMeUploadResponseDTO = new GuMeUploadResponseDTO();
        StringBuilder errorMessage = new StringBuilder();
        if (StringUtils.isBlank(uploadJsonDTO.getRequestId())) {
            errorMessage.append(",requestId不可为空");
        } else if (StringUtils.isBlank(uploadJsonDTO.getInstitutionCode())) {
            errorMessage.append(",institutionCode不可为空");
        } else if (uploadJsonDTO.getProperties() == null || uploadJsonDTO.getProperties().isEmpty()) {
            errorMessage.append(",properties不可为空");
        } else if (uploadJsonDTO.getUserList() == null || uploadJsonDTO.getUserList().isEmpty()) {
            errorMessage.append(",userList不可为空");
        }
        if (errorMessage.length() > 0) {
            guMeUploadResponseDTO.failed(GuMeUploadResponseDTO.ResultEnum.FAILED_FIELD_CHECK_ERROR, errorMessage.toString());
            return new CustomerResponseDTO(guMeUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, guMeUploadResponseDTO.getCode());
        } else {
            guMeUploadResponseDTO.success();
        }
        return new CustomerResponseDTO(guMeUploadResponseDTO, CustomerResponseDTO.StatusEnum.VALID, guMeUploadResponseDTO.getCode());
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
        GuMeUploadJsonDTO uploadJsonDTO = (GuMeUploadJsonDTO)adaptee;
        return uploadJsonDTO.getRequestId();
    }

    /**
     * 2023-10-23 17:37 获取业务数据量
     *
     * @param adaptee 客户定制数据
     * @return 传输的业务数据量
     */
    @Override
    public int countBizDataNumber(BaseUploadDataAdaptee adaptee) {
        GuMeUploadJsonDTO uploadJsonDTO = (GuMeUploadJsonDTO)adaptee;
        return uploadJsonDTO.getUserList() != null ? uploadJsonDTO.getUserList().size() : 0;
    }

    /**
     * 2023-10-24 19:24 获取全部的业务字段,用于检查是否有新增的字段
     *
     * @param jsonData 客户json字符串
     * @return 业务中要提示的新增字段
     */
    @Override
    public Set<String> getBizAllFields(String jsonData) {
        // TODO 强总处理@zeqiang.guo
        return Collections.emptySet();
    }

    /**
     * 2023-10-24 19:17 json解析错误,对应响应
     *
     * @param e 业务异常
     * @return 定制化客户响
     */
    @Override
    public CustomerResponseDTO jsonErrorResponse(Exception e) {
        GuMeUploadResponseDTO guMeUploadResponseDTO = new GuMeUploadResponseDTO();
        guMeUploadResponseDTO.failed(GuMeUploadResponseDTO.ResultEnum.FAILED_JSON_ERROR);
        return new CustomerResponseDTO(guMeUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, guMeUploadResponseDTO.getCode());
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
        GuMeUploadResponseDTO guMeUploadResponseDTO = new GuMeUploadResponseDTO();
        guMeUploadResponseDTO.failed();
        return new CustomerResponseDTO(guMeUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, guMeUploadResponseDTO.getCode());
    }
}
