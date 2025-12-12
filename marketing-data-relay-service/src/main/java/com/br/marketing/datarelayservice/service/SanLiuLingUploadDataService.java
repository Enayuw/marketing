package com.br.marketing.datarelayservice.service;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.sanliuling.request.CustomerInformationDTO;
import com.br.marketing.dto.sanliuling.request.SanLiuLingUploadRequestDTO;
import com.br.marketing.dto.sanliuling.response.SanLiuLingResponseDTO;
import com.br.marketing.entity.MarketingCustomerInitialData;
import com.br.marketing.entity.MarketingSanLiuLingCollection;
import com.br.marketing.enums.clean.DataCleanStatusEnum;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.MarketingCustomerInitialDataMapper;
import com.br.marketing.mapper.MarketingSanLiuLingCollectionMapper;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    MarketingSanLiuLingCollectionMapper marketingSanLiuLingCollectionMapper;
    @Resource
    MarketingCustomerInitialDataMapper marketingCustomerInitialDataMapper;
    @Resource
    private TrackingService trackingService;

    public SanLiuLingResponseDTO receiveCollectionUploadData(String jsonData, HttpServletRequest request) {
        SanLiuLingResponseDTO sanLiuLingResponseDTO = new SanLiuLingResponseDTO();
        sanLiuLingResponseDTO.success();
        try {
            JSONObject sanLiuLingCustomizeDataConfig = marketingCommonConfig.getSanLiuLingCustomizeDataConfig();
            String testApiCode = request.getHeader("Test-ApiCode");
            String apiCode = testApiCode != null ? testApiCode : sanLiuLingCustomizeDataConfig.getString("uploadApiCode");

            SanLiuLingUploadRequestDTO dto = JSONObject.parseObject(jsonData, SanLiuLingUploadRequestDTO.class);

            MarketingCustomerInitialData initialData = new MarketingCustomerInitialData();
            int size = 0;
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
                size = dto.getList().size();
            }
            String requestId = UUID.randomUUID().toString();
            if (errorMessage.length() > 0) {
                sanLiuLingResponseDTO = sanLiuLingResponseDTO.failed(SanLiuLingResponseDTO.ResultEnum.FAILED_PARAM_ERROR, errorMessage.toString());
                initialData.setStatus(Constants.STATUS_VOID);
                insertInitialData(apiCode, initialData, jsonData, requestId, size);
                return sanLiuLingResponseDTO;
            }
            //存储客户原始数据
            int i1 = insertInitialData(apiCode, initialData, jsonData, requestId, size);
            if (i1 != 1) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULINGCOLLECTION_SERVICEERROR.getCode(), "jsonData:" + jsonData,
                        "360催收定制上传数据入库失败(b_marketing_customer_initial_data)！！！"));
            }

            String taskId = dto.getTaskId();
            String batchNo = dto.getBatchNo();
            List<CustomerInformationDTO> list = dto.getList();
            //存储错误的客户id
            List<CustomerInformationDTO> failIdList = new ArrayList<>();
            //校验成功数据
            List<CustomerInformationDTO> successList = new ArrayList<>();
            // 校验客户列表中每个客户的必填参数
            for (int i = 0; i < list.size(); i++) {
                String customerErrors = validateCustomerInformation(list.get(i), i);
                if (!customerErrors.isEmpty()) {
                    log.warn("【360催收数据上传】参数校验失败, customerErrors: {}", customerErrors);
                    failIdList.add(list.get(i));
                } else {
                    successList.add(list.get(i));
                }
            }
            //处理失败数据
            if (!failIdList.isEmpty()) {
                log.warn("【360催收数据上传】参数校验失败: {}, jsonData: {}", failIdList, jsonData);
                insertOriginalData(apiCode, taskId, batchNo, 1, failIdList);
                List<String> idList = failIdList.stream()
                        .map(CustomerInformationDTO::getApplicationId)
                        .collect(Collectors.toList());
                sanLiuLingResponseDTO = sanLiuLingResponseDTO.failed(SanLiuLingResponseDTO.ResultEnum.FAILED_PARAM_ERROR, idList.toString());
            }
            //处理成功数据
            if (!successList.isEmpty()) {
                insertOriginalData(apiCode, taskId, batchNo,0, successList);
            }

            // 埋点
            try {
                JSONObject condition = new JSONObject();
                condition.put("taskId", taskId);
                trackingService.trackBusinessLog(DataFlowDirection.IN
                        , apiCode
                        , "360催收定制上传接口"
                        ,"b_sanliuling_collection_details"
                        , JSON.toJSONString(condition)
                        , Long.valueOf(successList.size())
                        , TrackingContext.generateBatchId());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULINGCOLLECTION_SERVICEERROR.getCode(), "jsonData:" + jsonData,
                    "360催收定制上传数据接入异常！！！"), e);
            sanLiuLingResponseDTO = sanLiuLingResponseDTO.failed(SanLiuLingResponseDTO.ResultEnum.FAILED_PARAM_ERROR);
        }
        log.warn("360催收数据上传接口被调用");
        return sanLiuLingResponseDTO;
    }

    private int insertInitialData(String apiCode, MarketingCustomerInitialData initialData,
                                  String jsonData, String requestId, int size) {
        initialData.setApiCode(apiCode);
        initialData.setRequestId(requestId);
        initialData.setJsonData(jsonData);
        initialData.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
        initialData.setAcceptType(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode());
        initialData.setActualNum(size);
        initialData.setReceiveDate(LocalDate.now().toString());
        return marketingCustomerInitialDataMapper.insertSelective(initialData);
    }

    private void insertOriginalData(String apiCode, String taskId, String batchNo,
                                    Integer isDelete, List<CustomerInformationDTO> successList) {

        List<MarketingSanLiuLingCollection> list = new ArrayList<>();

        for (CustomerInformationDTO dto : successList){
            MarketingSanLiuLingCollection marketingSanLiuLingCollection = new MarketingSanLiuLingCollection();
            marketingSanLiuLingCollection.setApiCode(apiCode);
            marketingSanLiuLingCollection.setTaskId(taskId);
            marketingSanLiuLingCollection.setBatchNo(batchNo);
            marketingSanLiuLingCollection.setApplicationId(dto.getApplicationId());
            marketingSanLiuLingCollection.setPhone(dto.getPhone());
            marketingSanLiuLingCollection.setSpeechParamSet(dto.getSpeechParamSet());
            marketingSanLiuLingCollection.setCustomerName(dto.getCustomerName());
            marketingSanLiuLingCollection.setCaseCode(dto.getCaseCode());
            marketingSanLiuLingCollection.setProductType(dto.getProductType());
            marketingSanLiuLingCollection.setPrologueRemark(dto.getPrologueRemark());
            marketingSanLiuLingCollection.setPhoneLabel(dto.getPhoneLabel());
            marketingSanLiuLingCollection.setCleanStatus(DataCleanStatusEnum.READY.getCode());
            marketingSanLiuLingCollection.setReceiveDate(LocalDate.now().toString());
            marketingSanLiuLingCollection.setIsDelete(isDelete);
            marketingSanLiuLingCollection.setCreateTime(new Date());
            marketingSanLiuLingCollection.setUpdateTime(new Date());
            list.add(marketingSanLiuLingCollection);
        }

        int i = marketingSanLiuLingCollectionMapper.batchInsert(list);
        if (i != 1) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULINGCOLLECTION_SERVICEERROR.getCode(), "jsonData:" + list,
                    "360催收定制上传数据入库失败(b_sanliuling_collection_details)！！！"));
        }
    }


    /**
     * 校验客户信息的必填参数
     *
     * @param customer 客户信息
     * @param index    客户在列表中的索引
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
