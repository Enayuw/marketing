package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.MarketingApiService;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
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
import com.br.marketing.vo.FileToMarketingFieldVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 数据清洗处理接口
 *
 * @Author: guangchao.zhang
 * @Date: 2024-07-11
 */
@Service
@Slf4j
public class DataCleaningAutoServiceImpl implements DataCleaningAutoService {

    @Resource
    MarketingDataFileConfigMapper marketingDataFileConfigMapper;

    @Resource
    PushInfoService pushInfoService;

    @Resource
    MarketingCleanDataTaskMapper marketingCleanDataTaskMapper;


    public static List<String> pattern = Arrays.asList(
            "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-M-dd",
            "yyyy-M-dd HH:mm:ss",
            "yyyy/M/dd",
            "yyyy/M/dd HH:mm:ss",
            "MM-dd-yyyy",
            "MM-dd-yyyy HH:mm:ss",
            "dd-MM-yyyy",
            "dd-MM-yyyy HH:mm:ss"
            // 可以添加更多可能的格式
    );

    @Override
    public void autoCleanDataByTask(MarketingCleanDataTask marketingCleanDataTask) {

        // 获取当前任务清洗数据所需要的配置
        MarketingDataFileConfig marketingDataFileConfig = marketingDataFileConfigMapper.selectByPrimaryKey(
                marketingCleanDataTask.getConfigId()
        );
        // 获取清洗数据
        String autoSearchDataSql = marketingDataFileConfig.getAutoSearchDataSql();
        String apiCode = marketingDataFileConfig.getApiCode();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5);
        while (true) {
            List<Map<String, Object>> cleanDataMapList = marketingDataFileConfigMapper.selectCleanData(autoSearchDataSql);
            if (cleanDataMapList.size() == 0) {
                break;
            }
            String autoTableName = marketingDataFileConfig.getAutoTableName();
            String autoDuplicateColumn = marketingDataFileConfig.getAutoDuplicateColumn();
            Integer cleanType = marketingDataFileConfig.getCleanType();
            Set<Object> collect = cleanDataMapList.stream().map(map -> map.get(autoDuplicateColumn)).collect(Collectors.toSet());
            marketingDataFileConfigMapper.updateCleanDataStatus(autoTableName, "1", autoDuplicateColumn, collect);
            List<FileToMarketingFieldVO> fieldVos = JSON.parseArray(
                    marketingDataFileConfig.getFieldConfig(),
                    FileToMarketingFieldVO.class
            );
            // 上传
            if (cleanType == 0) {
                processUploadCleanData(cleanDataMapList, fieldVos, apiCode, threadPool, collect, marketingDataFileConfig);
            }
            // 转化
            if (cleanType == 1) {
                processTransferCleanData(cleanDataMapList, fieldVos, apiCode, threadPool, collect, marketingDataFileConfig);
            }
        }
        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("清洗数据：线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.error("清洗数据：线程池结束异常！", ex);
            Thread.currentThread().interrupt();
        }
    }

    private void processTransferCleanData(List<Map<String, Object>> cleanDataMapList, List<FileToMarketingFieldVO> fieldVos,
                                          String apiCode, ThreadPoolExecutor threadPool, Set<Object> collect,
                                          MarketingDataFileConfig marketingDataFileConfig) {
        List<TransferDataItemDTO> transferDataItemDTOS = new ArrayList<>();
        for (int i = 0; i < cleanDataMapList.size(); i++) {
            Map<String, Object> cleanDataMap = cleanDataMapList.get(i);
            try {
                TransferDataItemDTO o = new TransferDataItemDTO();
                JSONObject reserveFieldJo = new JSONObject();
                cleanData(fieldVos, cleanDataMap, o, reserveFieldJo);
                if (reserveFieldJo.keySet().size() > 0) {
                    String reserveField1 = o.getReserveField1();
                    JSONObject parse = JSON.parseObject(reserveField1);
                    reserveFieldJo.putAll(parse);
                    o.setReserveField1(JSON.toJSONString(reserveFieldJo));
                }
                transferDataItemDTOS.add(o);
            } catch (Exception e) {
                log.error("上传清洗异常", e);
            }
        }
        asyncTransferData(transferDataItemDTOS, apiCode, threadPool, collect, marketingDataFileConfig);
    }

    private void processUploadCleanData(List<Map<String, Object>> cleanDataMapList, List<FileToMarketingFieldVO> fieldVos,
                                        String apiCode, ThreadPoolExecutor threadPool, Set<Object> collect,
                                        MarketingDataFileConfig marketingDataFileConfig) {
        List<MarketingPreUserDetailDTO> marketingPreUserDetailDTOS = new ArrayList<>();
        for (int i = 0; i < cleanDataMapList.size(); i++) {
            Map<String, Object> cleanDataMap = cleanDataMapList.get(i);
            try {
                MarketingPreUserDetailDTO o = new MarketingPreUserDetailDTO();
                JSONObject reserveFieldJo = new JSONObject();
                // 字段映射逻辑
                cleanData(fieldVos, cleanDataMap, o, reserveFieldJo);
                // 处理扩展
                if (reserveFieldJo.keySet().size() > 0) {
                    String reserveField1 = o.getReserveField1();
                    JSONObject parse = JSON.parseObject(reserveField1);
                    reserveFieldJo.putAll(parse);
                    o.setReserveField1(JSON.toJSONString(reserveFieldJo));
                }
                marketingPreUserDetailDTOS.add(o);
            } catch (Exception e) {
                log.error("转化清洗异常", e);
            }
        }
        asyncUploadDataNew(apiCode, threadPool, marketingPreUserDetailDTOS, collect, marketingDataFileConfig);
    }

    private <T> void cleanData(List<FileToMarketingFieldVO> fieldVos,
                               Map<String, Object> cleanDataMap, T o,
                               JSONObject reserveFieldJo) throws IOException, IllegalAccessException {
        Field[] declaredFields = o.getClass().getDeclaredFields();
        for (int f = 0; f < declaredFields.length; f++) {
            for (int j = 0; j < fieldVos.size(); j++) {
                FileToMarketingFieldVO fileToMarketingFieldVO = fieldVos.get(j);
                if (declaredFields[f].getName().equals(fileToMarketingFieldVO.getInterfaceField())) {
                    declaredFields[f].setAccessible(true);
                    Object fieldValue;
                    // 处理默认值
                    if (StringUtils.isNotBlank(fileToMarketingFieldVO.getDefaultValue())) {
                        fieldValue = fileToMarketingFieldVO.getDefaultValue();
                    } else {
                        fieldValue = cleanDataMap.get(fileToMarketingFieldVO.getHeadField());
                    }
                    // 时间格式转换
                    if (fileToMarketingFieldVO.getIsDateTransform()) {
                        fieldValue = getFormatterValue(String.valueOf(fieldValue));
                    }
                    // 处理字段转换 男 - > 1 女 -> 2
                    if (StringUtils.isNotBlank(fileToMarketingFieldVO.getConversion())) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        List<Map<String, String>> genderMappings = objectMapper.readValue(
                                fileToMarketingFieldVO.getConversion(), List.class
                        );
                        if (!genderMappings.isEmpty()) {
                            Map<String, String> genderMapping = genderMappings.get(0);
                            fieldValue = genderMapping.get(fieldValue);
                        }
                    }
                    declaredFields[f].set(o, fieldValue);
                    // 扩展字段容器
                    if (fileToMarketingFieldVO.getIsExtend()) {
                        reserveFieldJo.put(declaredFields[f].getName(), fieldValue);
                    }
                    break;
                }
            }
        }
    }

    /**
     * @param apiCode
     * @param cleanType  0 上传 1 转化
     * @param configName b_marketing_data_file_config 表中的rule_name 唯一
     */
    @Override
    public void saveCleanTask(String apiCode, Integer cleanType, String configName) {
        MarketingDataFileConfigExample mc = new MarketingDataFileConfigExample();
        mc.createCriteria().andApiCodeEqualTo(apiCode)
                .andRuleNameEqualTo(configName)
                .andIsDelEqualTo(1);
        List<MarketingDataFileConfig> marketingDataFileConfigs = marketingDataFileConfigMapper.selectByExample(mc);
        if (marketingDataFileConfigs.size() == 1) {
            MarketingDataFileConfig marketingDataFileConfig = marketingDataFileConfigs.get(0);
            //保存任务
            MarketingCleanDataTask task = new MarketingCleanDataTask();
            task.setConfigId(marketingDataFileConfig.getId());
            task.setCleanType(cleanType);
            task.setUpdateTime(new Date());
            task.setCleanStatus(0);
            task.setApiCode(apiCode);
            task.setCreateTime(new Date());
            marketingCleanDataTaskMapper.insertSelective(task);
        }else {
            log.error("清洗创建任务失败：{}",configName);
        }
    }


    /**
     * 异步调用上传数据接口
     *
     * @param apiCode   apiCode
     * @param threadPool  线程池
     * @param syncUsers 具体数据对象
     */
    private void asyncUploadDataNew(String apiCode
            , ThreadPoolExecutor threadPool, List<MarketingPreUserDetailDTO> syncUsers
            , Set<Object> collect, MarketingDataFileConfig marketingDataFileConfig) {
        String tasId = getTaskId(apiCode);
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(tasId);
        marketingPreUserDTO.setRequestId(tasId);
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        threadPool.submit(() -> {
            Result result = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
            updateStatus(collect, marketingDataFileConfig, result);
        });
    }

    /**
     * 异步调用转化数据接口
     *
     * @param apiCode  apiCode
     * @param threadPool 线程池
     */

    private void asyncTransferData(List<TransferDataItemDTO> transferDataItemDTOS, String apiCode,
                                   ThreadPoolExecutor threadPool, Set<Object> collect,
                                   MarketingDataFileConfig marketingDataFileConfig) {
        // 数据清洗
        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(transferDataItemDTOS);
        String taskId = getTaskId(apiCode);
        String requestId = taskId.concat("_").concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();
        transferDataDTO.setRequestId(requestId);
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        threadPool.submit(() -> {
            Result result = pushInfoService.pushTransferByRetry(dto, null);
            updateStatus(collect, marketingDataFileConfig, result);
        });
    }

    private void updateStatus(Set<Object> collect, MarketingDataFileConfig marketingDataFileConfig, Result result) {
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            marketingDataFileConfigMapper.updateCleanDataStatus(
                    marketingDataFileConfig.getAutoTableName(),
                    "2",
                    marketingDataFileConfig.getAutoDuplicateColumn(),
                    collect);
        } else {
            marketingDataFileConfigMapper.updateCleanDataStatus(
                    marketingDataFileConfig.getAutoTableName(),
                    "3",
                    marketingDataFileConfig.getAutoDuplicateColumn(),
                    collect);
        }
    }

    private static String getTaskId(String apiCode) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String taskId = apiCode.concat("_").concat(yyyyMMdd);
        return taskId;
    }

    /**
     * 处理时间格式的方法
     *
     * @param value 待处理的时间类型的值
     * @return String 格式化后的时间值（yyyy-MM-dd HH:mm:ss）
     */
    private String getFormatterValue(String value) {
        Date date = null;
        for (String parser : pattern) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(parser);
                date = sdf.parse(value);
                // 如果解析成功，则跳出循环
                break;
            } catch (ParseException e) {
                // 忽略异常，并尝试下一个解析器
                if (log.isInfoEnabled()) {
                    log.warn("无法解析日期;格式:{};原值:{}", parser, value);
                }
            }
        }
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            value = formatter.format(date);
        } else {
            log.error("无法解析日期:{}", value);
        }
        return value;
    }
}
