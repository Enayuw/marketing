package com.br.marketing.datarelayservice.service;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.datarelayservice.dto.smy.request.SmyUploadRequestDTO;
import com.br.marketing.datarelayservice.dto.smy.response.SmyResponseDTO;
import com.br.marketing.entity.CustomizeUploadDataSmy;
import com.br.marketing.mapper.CustomizeUploadDataSmyMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import java.time.LocalDate;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
     * @param dto smy upload request dto
     * @return {@link SmyResponseDTO }
     * @author Sion Cheng
     * @date 2024/12/18
     */
    public SmyResponseDTO receiveSmyUploadData(SmyUploadRequestDTO dto) {
        SmyResponseDTO smyResponseDTO = new SmyResponseDTO();
        smyResponseDTO.success();
        CustomizeUploadDataSmy customizeUploadDataSmy = new CustomizeUploadDataSmy();
        JSONObject smyCustomizeDataConfig = marketingCommonConfig.getSmyCustomizeDataConfig();
        customizeUploadDataSmy.setApiCode(smyCustomizeDataConfig == null ? null : smyCustomizeDataConfig.getString("uploadApiCode"));
        customizeUploadDataSmy.setRequestId(dto.getRequestNo());
        customizeUploadDataSmy.setReceiveDate(LocalDate.now().toString());
        customizeUploadDataSmy.setRequestJsonData(JSONObject.toJSONString(dto));
        customizeUploadDataSmy.setStatus(1);
        //Check Field
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
        customizeUploadDataSmy.setBizDataNumber(dto.getTotal());
        customizeUploadDataSmy.setResponseCode(String.valueOf(smyResponseDTO.getCode()));
        customizeUploadDataSmy.setResponseData(smyResponseDTO.getMessage());
        int i = customizeUploadDataSmyMapper.insertSelective(customizeUploadDataSmy);
        if (i != 1) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_UPLOAD_SERVICEERROR.getCode(), "dto:" + dto, "萨摩耶定制上传数据入库失败！！！"));
        }
        return smyResponseDTO;
    }
}
