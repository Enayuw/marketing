package com.br.marketing.datarelayservice.service;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.sanliuling.request.SanLiuLingUploadRequestDTO;
import com.br.marketing.dto.sanliuling.response.SanLiuLingResponseDTO;
import com.br.marketing.entity.MarketingCustomerOriginalData;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.UUID;

/**
 * @ClassName SanLiuLingUploadDataService
 * @Author kongbx
 * @Date 2025/8/28 14:19
 */
@Service
@Slf4j
public class SanLiuLingUploadDataService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    MarketingCustomerOriginalDataMapper marketingCustomerOriginalDataMapper;

    public SanLiuLingResponseDTO receiveCollectionUploadData(String jsonData, HttpServletRequest request) {
        SanLiuLingResponseDTO sanLiuLingResponseDTO = new SanLiuLingResponseDTO();
        sanLiuLingResponseDTO.success();
        try {
            JSONObject sanLiuLingCustomizeDataConfig = marketingCommonConfig.getSanLiuLingCustomizeDataConfig();
            String testApiCode = request.getHeader("Test-ApiCode");
            String apiCode = testApiCode != null ? testApiCode : sanLiuLingCustomizeDataConfig.getString("uploadApiCode");

            SanLiuLingUploadRequestDTO dto = JSONObject.parseObject(jsonData, SanLiuLingUploadRequestDTO.class);
            MarketingCustomerOriginalData originalData = new MarketingCustomerOriginalData();

            StringBuilder errorMessage = new StringBuilder();
            if (StringUtils.isBlank(dto.getTaskId())) {
                errorMessage.append(", taskId 不可为空");
            }
            if (StringUtils.isBlank(dto.getBatchNo())) {
                errorMessage.append(", batchNo 不可为空");
            }
            if (CollectionUtils.isEmpty(dto.getList())) {
                errorMessage.append(", 客户列表 不可为空");
            }

            if (errorMessage.length() > 0) {
                originalData.setStatus(0);
                sanLiuLingResponseDTO = sanLiuLingResponseDTO.failed(SanLiuLingResponseDTO.ResultEnum.FAILED_PARAM_ERROR, errorMessage.toString());
            }

            originalData.setApiCode(apiCode);
            originalData.setRequestId(UUID.randomUUID().toString());
            originalData.setJsonData(jsonData);
            originalData.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
            originalData.setAcceptType(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode());
            originalData.setActualNum(dto.getList().size());
            originalData.setReceiveDate(LocalDate.now().toString());
            int i = marketingCustomerOriginalDataMapper.insertSelective(originalData);
            if (i != 1) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULINGCOLLECTION_SERVICEERROR.getCode(), "jsonData:" + jsonData,
                        "360催收定制上传数据入库失败！！！"));
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULINGCOLLECTION_SERVICEERROR.getCode(), "jsonData:" + jsonData,
                    "360催收定制上传数据接入异常！！！"), e);
            sanLiuLingResponseDTO = sanLiuLingResponseDTO.failed(SanLiuLingResponseDTO.ResultEnum.FAILED_PARAM_ERROR);
        }
        log.warn("360催收数据上传接口被调用");
        return sanLiuLingResponseDTO;
    }


}
