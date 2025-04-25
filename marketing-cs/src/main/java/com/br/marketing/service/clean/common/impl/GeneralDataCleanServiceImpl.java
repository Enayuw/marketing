package com.br.marketing.service.clean.common.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingDataCleanConfig;
import com.br.marketing.mapper.MarketingDataCleanConfigMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.service.mark.DataMarkCommonService;
import com.br.marketing.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeneralDataCleanServiceImpl implements GeneralDataCleanService {

    @Resource
    private MarketingDataCleanConfigMapper marketingDataCleanConfigMapper;

    @Resource
    DataMarkCommonService dataMarkCommonService;

    @Resource
    private PushInfoService pushInfoService;

    private static final Integer CLEAN_TYPE_UPLOAD = 0;

    private static final Integer CLEAN_TYPE_TRANSFER = 1;

    private static final Integer ORIGIN_TYPE_BASIC = 1;

    private static final Integer ORIGIN_TYPE_EXTEND = 2;

    private static final Integer TARGET_TYPE_BASIC = 1;

    private static final Integer TARGET_TYPE_EXTEND = 2;

    private static final Integer MAPPING_MODE_MAPPING = 1;

    private static final Integer MAPPING_MODE_GROUP = 2;

    private static final Integer MAPPING_MODE_DEFAULT = 3;

    private static final String BIZ_ACTION = "common";

    @Override
    public Result uploadClean(List<JSONObject> data, String taksId, String apiCode){
        return this.uploadClean(data, taksId, apiCode, BIZ_ACTION);
    }

    @Override
    public Result transferClean(List<JSONObject> data, String apiCode) {
        return this.transferClean(data, apiCode, BIZ_ACTION);
    }

    @Override
    public Result uploadClean(List<JSONObject> data, String taksId, String apiCode, String bizAction){
        try {
            //1.查询清洗配置
            List<MarketingDataCleanConfig> configs = marketingDataCleanConfigMapper.selectConfigs(apiCode, CLEAN_TYPE_UPLOAD, bizAction);
            if (CollectionUtils.isEmpty(configs)) {
                log.warn("上传清洗未查询到配置，apiCode={}，bizAction={}", apiCode, bizAction);
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("未查询到清洗配置");
            }
            //2.清洗流程
            List<Pair<MarketingPreUserDetailDTO, JSONObject>> pairs = processData(data, configs, MarketingPreUserDetailDTO.class);
            List<MarketingPreUserDetailDTO> dtos =
                    pairs.stream().map(pair -> {
                        MarketingPreUserDetailDTO dto = pair.getLeft();
                        JSONObject extend = pair.getRight();
                        dto.setReserveField1(extend.toJSONString());
                        return dto;
                    }).collect(Collectors.toList());
            UploadDataDTO uploadDataDTO = initUploadData(dtos, apiCode);
            return pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        } catch (Exception e) {
            log.warn("通用上传清洗异常，apiCode={}，bizAction={}，e={}", apiCode, bizAction, e);
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("上传清洗流程出现异常");
        }
    }

    @Override
    public Result transferClean(List<JSONObject> data, String apiCode, String bizAction) {
        try {
            //1.查询清洗配置
            List<MarketingDataCleanConfig> configs = marketingDataCleanConfigMapper.selectConfigs(apiCode, CLEAN_TYPE_TRANSFER, bizAction);
            if (CollectionUtils.isEmpty(configs)) {
                log.warn("转化清洗未查询到配置，apiCode={}，bizAction={}", apiCode, bizAction);
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("未查询到清洗配置");
            }
            //2.清洗流程
            List<Pair<TransferDataItemDTO, JSONObject>> pairs = processData(data, configs, TransferDataItemDTO.class);
            List<TransferDataItemDTO> dtos =
                    pairs.stream().map(pair -> {
                        TransferDataItemDTO dto = pair.getLeft();
                        JSONObject extend = pair.getRight();
                        dto.setReserveField1(extend.toJSONString());
                        return dto;
                    }).collect(Collectors.toList());
            PushTransferDataDetailDTO pushTransferDataDetailDTO = initTransferData(dtos, apiCode);
            return pushInfoService.pushTransferByRetry(pushTransferDataDetailDTO, null);
        } catch (Exception e) {
            log.warn("通用上传清洗异常，apiCode={}，bizAction={}，e={}", apiCode, bizAction, e);
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("上传清洗流程出现异常");
        }
    }

    private static Map<String, Field> getStringFieldMap(List<MarketingDataCleanConfig> configs) throws NoSuchFieldException {
        //2.基础字段集合
        Set<String> basicTargetNames = configs.stream()
                .filter(config -> config.getTargetType() == TARGET_TYPE_BASIC)
                .map(MarketingDataCleanConfig::getTargetName)
                .collect(Collectors.toSet());
        Map<String, Field> fieldMap = new HashMap<>();
        for (String basicTargetName : basicTargetNames) {
            Field declaredField = MarketingPreUserDetailDTO.class.getDeclaredField(basicTargetName);
            declaredField.setAccessible(true);
            fieldMap.put(basicTargetName, declaredField);
        }
        return fieldMap;
    }

    private <T> List<Pair<T, JSONObject>> processData(List<JSONObject> data, List<MarketingDataCleanConfig> configs, Class<T> dtoClass) throws NoSuchFieldException {
        Map<String, Field> fieldMap = getStringFieldMap(configs);
        Map<String, List<MarketingDataCleanConfig>> configsGroup = configs.stream()
                .collect(Collectors.groupingBy(MarketingDataCleanConfig::getTargetName));
        return data.parallelStream()
                .map(datum -> {
                    try {
                        return processSingleRecord(datum, configsGroup, fieldMap, dtoClass);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private <T> Pair<T, JSONObject> processSingleRecord(JSONObject datum, Map<String, List<MarketingDataCleanConfig>> configsGroup, Map<String, Field> fieldMap, Class<T> dtoClass) throws InstantiationException, IllegalAccessException {
        T dto = dtoClass.newInstance();
        JSONObject extend = extendStandardizat(datum);
        datum.putAll(extend);
        configsGroup.forEach((targetName, configList) -> {
            Object fieldValue = null;
            MarketingDataCleanConfig configExample = configList.get(0);
            if(configExample.getMappingMode() == MAPPING_MODE_MAPPING){
                //取值
                if (configExample.getOriginType() == ORIGIN_TYPE_BASIC) {
                    fieldValue = datum.get(configExample.getOriginName());
                } else if (configExample.getOriginType() == ORIGIN_TYPE_EXTEND) {
                    fieldValue = extend.get(configExample.getOriginName());
                }
                //格式化
                fieldValue = fieldFormat(fieldValue, configExample);
                //赋值
                if(configExample.getTargetType() == TARGET_TYPE_BASIC){
                    Field field = fieldMap.get(configExample.getTargetName());
                    field.setAccessible(true);
                    try {
                        field.set(dto, fieldValue);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                } else if (configExample.getTargetType() == TARGET_TYPE_EXTEND) {
                    extend.put(configExample.getTargetName(), fieldValue);
                }
            } else if (configExample.getMappingMode() == MAPPING_MODE_GROUP) {
                for (MarketingDataCleanConfig config : configList) {
                    if (dataMarkCommonService.isMatch(new HashMap<>(datum), config.getMappingCondition())) {
                        if (config.getOriginType() != null) {
                            if (config.getOriginType() == ORIGIN_TYPE_BASIC) {
                                fieldValue = datum.get(config.getOriginName());
                            } else if (config.getOriginType() == ORIGIN_TYPE_EXTEND) {
                                fieldValue = extend.get(config.getOriginName());
                            }
                            //格式化
                            fieldValue = fieldFormat(fieldValue, configExample);
                        } else {
                            fieldValue = config.getMappingOutValue();
                        }
                        //赋值
                        if(config.getTargetType() == TARGET_TYPE_BASIC){
                            Field field = fieldMap.get(config.getTargetName());
                            field.setAccessible(true);
                            try {
                                field.set(dto, ConvertUtils.convert(fieldValue, field.getType()));
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (config.getTargetType() == TARGET_TYPE_EXTEND) {
                            extend.put(config.getTargetName(), fieldValue);
                        }
                    }
                }
            } else if (configExample.getMappingMode() == MAPPING_MODE_DEFAULT) {
                fieldValue = configExample.getDefaultValue();
                if(configExample.getTargetType() == TARGET_TYPE_BASIC){
                    Field field = fieldMap.get(configExample.getTargetName());
                    field.setAccessible(true);
                    try {
                        field.set(dto, ConvertUtils.convert(fieldValue, field.getType()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                } else if (configExample.getTargetType() == TARGET_TYPE_EXTEND) {
                    extend.put(configExample.getTargetName(), fieldValue);
                }
            }
        });
        return ImmutablePair.of(dto, extend);
    }

    /**
     * @description 格式化
     * @param fieldValue
     * @param config
     * @return void
     * @author hedongshuo
     * @date 2025/4/24 17:15
     **/
    private Object fieldFormat(Object fieldValue, MarketingDataCleanConfig config) {
        if(fieldValue == null) {
            return null;
        }
        if (StringUtils.isNotBlank(config.getConversion())) {
            JSONObject conversion = JSONObject.parseObject(config.getConversion());
            return conversion.get(fieldValue.toString());
        }
        if (StringUtils.isNotBlank(config.getDateTransformPattern())) {
            return TimeUtils.getFormatterValue(fieldValue.toString(), config.getDateTransformPattern());
        }
        if (config.getDecimalReserveType() != null) {

        }
        return fieldValue;
    }

    /**
     * @param datum
     * @return void
     * @description 扩展标准化，将原始数据中的json集成为extend
     * @author hedongshuo
     * @date 2025/4/24 16:13
     **/
    private JSONObject extendStandardizat(JSONObject datum) {
        JSONObject extend = new JSONObject();
        datum.forEach((key, value) -> {
            if (null != value && StringUtils.isJson(value.toString())) {
                extend.putAll(JSONObject.parseObject(value.toString()));
            }
        });
        return extend;
    }

    private static String getRequestId(String taskId) {
        return taskId.concat("_").concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();
    }

    private static String getTaskId(String apiCode) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return apiCode.concat("_").concat(yyyyMMdd);
    }

    private UploadDataDTO initUploadData(List<MarketingPreUserDetailDTO> syncUsers, String apiCode) {
        String taskId = getTaskId(apiCode);
        String requestId = getRequestId(taskId);
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(taskId);
        marketingPreUserDTO.setRequestId(requestId);
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }

    private PushTransferDataDetailDTO initTransferData(List<TransferDataItemDTO> transferDataItemDTOS, String apiCode) {
        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        TransferDataDTO<TransferDataItemDTO> transferDataDTO = new TransferDataDTO<>();
        transferDataDTO.setDataItems(transferDataItemDTOS);
        String taskId = getTaskId(apiCode);
        String requestId = getRequestId(taskId);
        transferDataDTO.setRequestId(requestId);
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        return dto;
    }
}
