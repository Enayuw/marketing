package com.br.marketing.datarelayservice.service;

import java.time.LocalDate;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.datarelayservice.dto.smy.request.SmyUploadRequestDTO;
import com.br.marketing.datarelayservice.dto.smy.response.SmyResponseDTO;
import com.br.marketing.entity.CustomizeUploadDataSmy;
import com.br.marketing.mapper.CustomizeUploadDataSmyMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * smy upload data service
 *
 * @author Sion.Cheng
 * @date 2024/12/18
 */
@Service
@Slf4j
public class SmyUploadDataService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private CustomizeUploadDataSmyMapper customizeUploadDataSmyMapper;

    /**
     * receive smy upload data
     *
     * @param jsonData json data
     * @return {@link SmyResponseDTO }
     * @author senyang.zheng
     * @date 2024/12/27
     */
    public SmyResponseDTO receiveSmyUploadData(String jsonData) {
        SmyResponseDTO smyResponseDTO = new SmyResponseDTO();
        smyResponseDTO.success();
        try {
            SmyUploadRequestDTO dto = JSONObject.parseObject(jsonData, SmyUploadRequestDTO.class);
            CustomizeUploadDataSmy customizeUploadDataSmy = new CustomizeUploadDataSmy();
            JSONObject smyCustomizeDataConfig = marketingCommonConfig.getSmyCustomizeDataConfig();
            customizeUploadDataSmy.setApiCode(smyCustomizeDataConfig == null ? null : smyCustomizeDataConfig.getString("uploadApiCode"));
            customizeUploadDataSmy.setRequestId(dto.getRequestNo());
            customizeUploadDataSmy.setReceiveDate(LocalDate.now().toString());
            customizeUploadDataSmy.setRequestJsonData(jsonData);
            customizeUploadDataSmy.setStatus(1);
            // Check Field
            StringBuilder errorMessage = new StringBuilder();
            if (StringUtils.isBlank(dto.getRequestNo())) {
                errorMessage.append(", request_no 不可为空");
            } else if (StringUtils.isBlank(dto.getCaseType())) {
                errorMessage.append(", case_type 不可为空");
            } else if (dto.getTotal() == null) {
                errorMessage.append(", total 不可为空");
            } else if (dto.getNameList() == null || dto.getNameList().isEmpty()) {
                errorMessage.append(", name_list 不可为空");
            }
            if (errorMessage.length() > 0) {
                customizeUploadDataSmy.setStatus(0);
                smyResponseDTO = smyResponseDTO.failed(SmyResponseDTO.ResultEnum.FAILED_PARAM_ERROR, errorMessage.toString());
            }
            if (dto.getTotal() != dto.getNameList().size()) {
                customizeUploadDataSmy.setStatus(0);
                smyResponseDTO = smyResponseDTO.failed(SmyResponseDTO.ResultEnum.FAILED_BIZ_ERROR, "批次总数与代运营名单列表条数不符");
            }
            customizeUploadDataSmy.setBizDataNumber(dto.getTotal());
            customizeUploadDataSmy.setResponseCode(String.valueOf(smyResponseDTO.getCode()));
            customizeUploadDataSmy.setResponseData(smyResponseDTO.getMessage());
            int i = customizeUploadDataSmyMapper.insertSelective(customizeUploadDataSmy);
            if (i != 1) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_UPLOAD_SERVICEERROR.getCode(), "jsonData:" + jsonData,
                    "萨摩耶定制上传数据入库失败！！！"));
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_UPLOAD_SERVICEERROR.getCode(), "jsonData:" + jsonData,
                "萨摩耶定制上传数据接入异常！！！"));
            smyResponseDTO = smyResponseDTO.failed(SmyResponseDTO.ResultEnum.FAILED_SYSTEM_ERROR);
        }
        return smyResponseDTO;
    }
}
