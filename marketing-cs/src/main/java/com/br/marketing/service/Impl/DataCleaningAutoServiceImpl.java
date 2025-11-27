package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.MarketingDataFileConfig;
import com.br.marketing.entity.MarketingDataFileConfigExample;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.MarketingDataFileConfigMapper;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.util.TimeUtils;
import com.br.marketing.vo.FileToMarketingFieldVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 数据清洗处理接口
 * {@code @Author:} guangchao.zhang
 * {@code @Date:} 2024-07-11
 */
@Service
@Slf4j
public class DataCleaningAutoServiceImpl implements DataCleaningAutoService {

    public static final String CLEAN_STATUS_1 = "1";
    public static final String CLEAN_STATUS_2 = "2";
    public static final String CLEAN_STATUS_3 = "3";
    public static final int CLEAN_TYPE_UPLOAD = 0;
    public static final int CLEAN_TYPE_TRANSFER = 1;

    @Resource
    private MarketingDataFileConfigMapper marketingDataFileConfigMapper;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    private MarketingCleanDataTaskMapper marketingCleanDataTaskMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void autoCleanDataByTask(MarketingCleanDataTask marketingCleanDataTask) {
        try {
            doAutoCleanData(marketingCleanDataTask);
            // 更新任务为清洗完成
            marketingCleanDataTask.setCleanStatus(2);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), "清洗任务异常！"), e);
            // 更新任务为清洗完成
            marketingCleanDataTask.setCleanStatus(3);
        }
        marketingCleanDataTaskMapper.updateByPrimaryKeySelective(marketingCleanDataTask);
    }

    private void doAutoCleanData(MarketingCleanDataTask marketingCleanDataTask) {
        MarketingDataFileConfig marketingDataFileConfig = marketingDataFileConfigMapper.selectByPrimaryKey(
                marketingCleanDataTask.getConfigId()
        );
        String autoSearchDataSql = marketingDataFileConfig.getAutoSearchDataSql();
        String apiCode = marketingDataFileConfig.getApiCode();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(
                marketingCommonConfig.getAutoCleanDataThreadNum(),
                marketingCommonConfig.getAutoCleanDataThreadNum()
        );
        while (true) {
            modifyThreadPool(threadPool);
            List<Map<String, Object>> cleanDataMapList = marketingDataFileConfigMapper.selectCleanData(
                    autoSearchDataSql, marketingCleanDataTask.getId()
            );
            if (cleanDataMapList.isEmpty()) {
                break;
            }
            String autoTableName = marketingDataFileConfig.getAutoTableName();
            String autoDuplicateColumn = marketingDataFileConfig.getAutoDuplicateColumn();
            Integer cleanType = marketingDataFileConfig.getCleanType();
            Set<Object> collect = cleanDataMapList.stream().map(map -> map.get(autoDuplicateColumn)).collect(Collectors.toSet());
            marketingDataFileConfigMapper.updateCleanDataStatus(autoTableName, CLEAN_STATUS_1, autoDuplicateColumn, collect);
            threadPool.submit(() -> {
                if (cleanType == CLEAN_TYPE_UPLOAD) {
                    doProcessUploadDataClean(cleanDataMapList, marketingDataFileConfig, apiCode, collect);
                }
                if (cleanType == CLEAN_TYPE_TRANSFER) {
                    doProcessTransferDataClean(cleanDataMapList, marketingDataFileConfig, apiCode, collect);
                }
            });

        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), "清洗数据线程池结束异常！"));
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), "清洗数据：线程池结束异常！！"), ex);
            Thread.currentThread().interrupt();
        }
    }

    private void modifyThreadPool(ThreadPoolExecutor threadPool) {
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, marketingCommonConfig.getAutoCleanDataThreadNum());
    }

    private void doProcessUploadDataClean(
            List<Map<String, Object>> cleanDataMapList,
            MarketingDataFileConfig marketingDataFileConfig,
            String apiCode,
            Set<Object> collect) {
        try {
            // 上传数据处理
            List<MarketingPreUserDetailDTO> marketingPreUserDetailDTOS = processUploadCleanData(cleanDataMapList, marketingDataFileConfig);
            // 上传数据组装
            UploadDataDTO uploadDataDTO = initUploadData(apiCode, marketingPreUserDetailDTOS);
            // 上传数据异步推送
            pushAsyncUploadData(uploadDataDTO, collect, marketingDataFileConfig);
        } catch (IOException | IllegalAccessException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                    "上传数据清洗数据异常！！"), e);
        }

    }

    private void doProcessTransferDataClean(
            List<Map<String, Object>> cleanDataMapList,
            MarketingDataFileConfig marketingDataFileConfig,
            String apiCode,
            Set<Object> collect) {
        try {
            // 转化数据处理
            List<TransferDataItemDTO> transferDataItemDTOS = processTransferCleanData(cleanDataMapList, marketingDataFileConfig);
            // 转化数据组装
            PushTransferDataDetailDTO pushTransferDataDetailDTO = initTransferData(transferDataItemDTOS, apiCode);
            // 转化数据异步推送
            pushAsyncTransferData(pushTransferDataDetailDTO, collect, marketingDataFileConfig);
        } catch (IOException | IllegalAccessException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                    "转化数据清洗数据异常！！"), e);
        }

    }

    private void pushAsyncUploadData(UploadDataDTO uploadDataDTO, Set<Object> collect, MarketingDataFileConfig marketingDataFileConfig) {
        Result<Boolean> result = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        updateStatus(collect, marketingDataFileConfig, result);
    }

    private void pushAsyncTransferData(
            PushTransferDataDetailDTO pushTransferDataDetailDTO,
            Set<Object> collect,
            MarketingDataFileConfig marketingDataFileConfig) {
        Result<Boolean> result = pushInfoService.pushTransferByRetry(pushTransferDataDetailDTO, null);
        updateStatus(collect, marketingDataFileConfig, result);
    }

    private List<MarketingPreUserDetailDTO> processUploadCleanData(
            List<Map<String, Object>> cleanDataMapList,
            MarketingDataFileConfig marketingDataFileConfig) throws IOException, IllegalAccessException {
        List<MarketingPreUserDetailDTO> marketingPreUserDetailDTOS = new ArrayList<>();
        for (Map<String, Object> cleanDataMap : cleanDataMapList) {
            MarketingPreUserDetailDTO o = new MarketingPreUserDetailDTO();
            JSONObject reserveFieldJo = new JSONObject();
            cleanData(marketingDataFileConfig, cleanDataMap, o, reserveFieldJo);
            uploadExtendData(reserveFieldJo, o);
            marketingPreUserDetailDTOS.add(o);
        }
        return marketingPreUserDetailDTOS;
    }

    private List<TransferDataItemDTO> processTransferCleanData(
            List<Map<String, Object>> cleanDataMapList,
            MarketingDataFileConfig marketingDataFileConfig) throws IOException, IllegalAccessException {
        List<TransferDataItemDTO> transferDataItemDTOS = new ArrayList<>();
        for (Map<String, Object> cleanDataMap : cleanDataMapList) {
            TransferDataItemDTO o = new TransferDataItemDTO();
            JSONObject reserveFieldJo = new JSONObject();
            cleanData(marketingDataFileConfig, cleanDataMap, o, reserveFieldJo);
            transferExtendData(reserveFieldJo, o);
            transferDataItemDTOS.add(o);
        }
        return transferDataItemDTOS;

    }

    private void uploadExtendData(JSONObject reserveFieldJo, MarketingPreUserDetailDTO o) {
        if (!reserveFieldJo.keySet().isEmpty()) {
            String reserveField1 = o.getReserveField1();
            getReserveFiledValue(reserveFieldJo, reserveField1);
            o.setReserveField1(JSON.toJSONString(reserveFieldJo));
        }
    }

    private void transferExtendData(JSONObject reserveFieldJo, TransferDataItemDTO o) {
        if (!reserveFieldJo.keySet().isEmpty()) {
            String reserveField1 = o.getReserveField1();
            getReserveFiledValue(reserveFieldJo, reserveField1);
            o.setReserveField1(JSON.toJSONString(reserveFieldJo));
        }
    }

    private static void getReserveFiledValue(JSONObject reserveFieldJo, String reserveField1) {
        if (StringUtils.isNotBlank(reserveField1)) {
            JSONObject parse = JSON.parseObject(reserveField1);
            reserveFieldJo.putAll(parse);
        }
    }

    private <T> void cleanData(MarketingDataFileConfig marketingDataFileConfig,
                               Map<String, Object> cleanDataMap, T o,
                               JSONObject reserveFieldJo) throws IOException, IllegalAccessException {
        List<FileToMarketingFieldVO> fieldVos = JSON.parseArray(
                marketingDataFileConfig.getFieldConfig(),
                FileToMarketingFieldVO.class
        );
        Field[] declaredFields = o.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            for (FileToMarketingFieldVO fileToMarketingFieldVO : fieldVos) {
                // 扩展字段容器
                if (fileToMarketingFieldVO.getIsExtend()) {
                    Object fieldValue = fieldMapping(cleanDataMap, fileToMarketingFieldVO);
                    reserveFieldJo.put(fileToMarketingFieldVO.getInterfaceField(), fieldValue);
                } else if (declaredField.getName().equals(fileToMarketingFieldVO.getInterfaceField())) {
                    declaredField.setAccessible(true);
                    Object fieldValue = fieldMapping(cleanDataMap, fileToMarketingFieldVO);
                    declaredField.set(o, fieldValue);
                    break;
                }
            }
        }
    }

    private static Object fieldMapping(
            Map<String, Object> cleanDataMap,
            FileToMarketingFieldVO fileToMarketingFieldVO) throws IOException {
        Object fieldValue;
        // 处理默认值
        fieldValue = cleanDataMap.get(fileToMarketingFieldVO.getHeadField());
        if (fieldValue == null) {
            fieldValue = fileToMarketingFieldVO.getDefaultValue();
        }
        // 时间格式转换
        if (fileToMarketingFieldVO.getIsDateTransform()) {
            fieldValue = TimeUtils.getFormatterValue(
                    String.valueOf(fieldValue),
                    fileToMarketingFieldVO.getDateTransformPattern()
            );
        }
        // 处理字段转换 男 - > 1 女 -> 2
        if (StringUtils.isNotBlank(fileToMarketingFieldVO.getConversion())) {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, String>> genderMappings = objectMapper.readValue(
                    fileToMarketingFieldVO.getConversion(), List.class
            );
            if (!genderMappings.isEmpty()) {
                Map<String, String> genderMapping = genderMappings.get(0);
                if (fieldValue != null) {
                    fieldValue = genderMapping.get(fieldValue);
                }
            }
        }
        return fieldValue;
    }

    /**
     * 异步调用上传数据接口
     *
     * @param apiCode   apiCode
     * @param syncUsers 具体数据对象
     */
    private UploadDataDTO initUploadData(String apiCode, List<MarketingPreUserDetailDTO> syncUsers) {
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

    private static String getRequestId(String taskId) {
        return taskId.concat("_").concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();
    }

    /**
     * 异步调用转化数据接口
     *
     * @param apiCode apiCode
     */

    private PushTransferDataDetailDTO initTransferData(List<TransferDataItemDTO> transferDataItemDTOS, String apiCode) {
        // 数据清洗
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

    private void updateStatus(Set<Object> collect, MarketingDataFileConfig marketingDataFileConfig, Result<Boolean> result) {
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            marketingDataFileConfigMapper.updateCleanDataStatus(
                    marketingDataFileConfig.getAutoTableName(),
                    CLEAN_STATUS_2,
                    marketingDataFileConfig.getAutoDuplicateColumn(),
                    collect);
        } else {
            marketingDataFileConfigMapper.updateCleanDataStatus(
                    marketingDataFileConfig.getAutoTableName(),
                    CLEAN_STATUS_3,
                    marketingDataFileConfig.getAutoDuplicateColumn(),
                    collect);
        }
    }

    private static String getTaskId(String apiCode) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return apiCode.concat("_").concat(yyyyMMdd);
    }


    /**
     * @param apiCode    apiCode
     * @param cleanType  0 上传 1 转化
     * @param configName b_marketing_data_file_config 表中的rule_name 唯一
     */
    @Override
    public Long saveCleanTask(String apiCode, Integer cleanType, String configName) {
        MarketingDataFileConfigExample mc = new MarketingDataFileConfigExample();
        mc.createCriteria().andApiCodeEqualTo(apiCode)
                .andRuleNameEqualTo(configName)
                .andIsDelEqualTo(1);
        List<MarketingDataFileConfig> marketingDataFileConfigs = marketingDataFileConfigMapper.selectByExample(mc);
        if (marketingDataFileConfigs.size() == 1) {
            MarketingDataFileConfig marketingDataFileConfig = marketingDataFileConfigs.get(0);
            MarketingCleanDataTask task = new MarketingCleanDataTask();
            task.setConfigId(marketingDataFileConfig.getId());
            task.setCleanType(cleanType);
            task.setUpdateTime(new Date());
            task.setApiCode(apiCode);
            task.setCreateTime(new Date());
            task.setAutoCleanWayType(1);
            marketingCleanDataTaskMapper.insertSelective(task);
            return task.getId();
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                    "清洗任务生成时未找到清洗对应的配置！！"));
        }
        return null;
    }
}
