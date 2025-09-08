package com.br.marketing.datarelayservice.service;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.sanliuling.request.CustomerInformationDTO;
import com.br.marketing.dto.sanliuling.request.SanLiuLingUploadRequestDTO;
import com.br.marketing.dto.sanliuling.response.SanLiuLingResponseDTO;
import com.br.marketing.entity.MarketingCustomerOriginalData;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    PushRuleService pushRuleService;
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

            // 完整的参数校验
            StringBuilder errorMessage = new StringBuilder();
            
            // 校验主要参数
            if (StringUtils.isBlank(dto.getTaskId())) {
                errorMessage.append("taskId不可为空; ");
            }
            if (StringUtils.isBlank(dto.getBatchNo())) {
                errorMessage.append("batchNo不可为空; ");
            }
            if (CollectionUtils.isEmpty(dto.getList())) {
                errorMessage.append("客户列表不可为空; ");
            } else {
                // 校验客户列表中每个客户的必填参数
                for (int i = 0; i < dto.getList().size(); i++) {
                    String customerErrors = validateCustomerInformation(dto.getList().get(i), i);
                    if (!customerErrors.isEmpty()) {
                        errorMessage.append(customerErrors);
                    }
                }
            }

            if (errorMessage.length() > 0) {
                originalData.setStatus(0);
                String finalErrorMessage = errorMessage.toString().trim();
                // 移除最后的分号和空格
                if (finalErrorMessage.endsWith("; ")) {
                    finalErrorMessage = finalErrorMessage.substring(0, finalErrorMessage.length() - 2);
                }
                log.warn("【360催收数据上传】参数校验失败: {}, jsonData: {}", finalErrorMessage, jsonData);
                sanLiuLingResponseDTO = sanLiuLingResponseDTO.failed(SanLiuLingResponseDTO.ResultEnum.FAILED_PARAM_ERROR, finalErrorMessage);
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
            //只有校验成功的数据才会解析
            if(sanLiuLingResponseDTO.getCode().equals(SanLiuLingResponseDTO.ResultEnum.SUCCESS.getCode())){
                pushRuleService.sendCustomJsonParseMq(apiCode, originalData.getId());
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULINGCOLLECTION_SERVICEERROR.getCode(), "jsonData:" + jsonData,
                    "360催收定制上传数据接入异常！！！"), e);
            sanLiuLingResponseDTO = sanLiuLingResponseDTO.failed(SanLiuLingResponseDTO.ResultEnum.FAILED_PARAM_ERROR);
        }
        log.warn("360催收数据上传接口被调用");
        return sanLiuLingResponseDTO;
    }

    /**
     * 校验客户信息的必填参数
     * 
     * @param customer 客户信息
     * @param index 客户在列表中的索引
     * @return 错误信息，如果没有错误返回空字符串
     */
    private String validateCustomerInformation(CustomerInformationDTO customer, int index) {
        if (customer == null) {
            return String.format("客户列表第%d项为空; ", index + 1);
        }
        
        StringBuilder errors = new StringBuilder();
        String customerPrefix = String.format("客户列表第%d项", index + 1);
        
        // 校验必填字段
        if (StringUtils.isBlank(customer.getApplicationId())) {
            errors.append(String.format("%s applicationId不可为空; ", customerPrefix));
        }
        
        if (StringUtils.isBlank(customer.getPhone())) {
            errors.append(String.format("%s phone不可为空; ", customerPrefix));
        }
        
        if (StringUtils.isBlank(customer.getSpeechParamSet())) {
            errors.append(String.format("%s speechParamSet不可为空; ", customerPrefix));
        } else {
            // 校验speechParamSet格式是否为有效JSON
            if (!isValidJsonString(customer.getSpeechParamSet())) {
                errors.append(String.format("%s speechParamSet格式不正确，应为JSON字符串; ", customerPrefix));
            } else {
                // 校验speechParamSet中的必填字段
                String speechParamErrors = validateSpeechParamSet(customer.getSpeechParamSet(), customerPrefix);
                if (!speechParamErrors.isEmpty()) {
                    errors.append(speechParamErrors);
                }
            }
        }
        
        if (StringUtils.isBlank(customer.getCustomerName())) {
            errors.append(String.format("%s customerName不可为空; ", customerPrefix));
        }
        
        if (StringUtils.isBlank(customer.getCaseCode())) {
            errors.append(String.format("%s caseCode不可为空; ", customerPrefix));
        }
        
        if (StringUtils.isBlank(customer.getProductType())) {
            errors.append(String.format("%s productType不可为空; ", customerPrefix));
        }
        
        if (StringUtils.isBlank(customer.getPrologueRemark())) {
            errors.append(String.format("%s prologueRemark不可为空; ", customerPrefix));
        }
        
        if (StringUtils.isBlank(customer.getPhoneLabel())) {
            errors.append(String.format("%s phoneLabel不可为空; ", customerPrefix));
        }
        
        return errors.toString();
    }
    
    /**
     * 校验speechParamSet中的必填参数
     * 
     * @param speechParamSet JSON字符串
     * @param customerPrefix 客户前缀（用于错误信息）
     * @return 错误信息，如果没有错误返回空字符串
     */
    private String validateSpeechParamSet(String speechParamSet, String customerPrefix) {
        try {
            JSONObject speechParams = JSONObject.parseObject(speechParamSet);
            StringBuilder errors = new StringBuilder();
            
            // 校验必填字段
            if (!speechParams.containsKey("name") || StringUtils.isBlank(speechParams.getString("name"))) {
                errors.append(String.format("%s speechParamSet中name不可为空; ", customerPrefix));
            }

            if (!speechParams.containsKey("sex") || StringUtils.isBlank(speechParams.getString("sex"))) {
                errors.append(String.format("%s speechParamSet中sex不可为空; ", customerPrefix));
            }

            if (!speechParams.containsKey("money") || StringUtils.isBlank(speechParams.getString("money"))) {
                errors.append(String.format("%s speechParamSet中money不可为空; ", customerPrefix));
            }
            
            if (!speechParams.containsKey("overdue_date") || StringUtils.isBlank(speechParams.getString("overdue_date"))) {
                errors.append(String.format("%s speechParamSet中overdue_date不可为空; ", customerPrefix));
            }
            
            if (!speechParams.containsKey("overdue_days") || StringUtils.isBlank(speechParams.getString("overdue_days"))) {
                errors.append(String.format("%s speechParamSet中overdue_days不可为空; ", customerPrefix));
            }
            return errors.toString();
        } catch (Exception e) {
            return String.format("%s speechParamSet JSON解析失败; ", customerPrefix);
        }
    }
    
    /**
     * 检查字符串是否为有效的JSON格式
     * 
     * @param jsonString 待检查的字符串
     * @return true如果是有效JSON，否则false
     */
    private boolean isValidJsonString(String jsonString) {
        if (StringUtils.isBlank(jsonString)) {
            return false;
        }
        try {
            JSONObject.parseObject(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
