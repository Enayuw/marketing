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
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.MarketingDataFileConfigMapper;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.service.IDataCleaningGeneralService;
import com.br.marketing.service.IFileToMarketingRuleService;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import com.br.marketing.vo.FileToMarketingFieldVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

import static com.br.common.util.DateUtils.yyyyMMdd;

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


    @Resource
    MarketingApiService marketingApiService;


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
        // 执行当前配置锁对应的清洗需求
        // while 循环执行清洗查询的sql
        // 获取到数据后，根据配置的的headerFiled 字段，获取数据
        // 根据配置的映射组装数据

        // 获取当前任务清洗数据所需要的配置
        MarketingDataFileConfig marketingDataFileConfig = marketingDataFileConfigMapper.selectByPrimaryKey(
                marketingCleanDataTask.getConfigId()
        );
        // 获取清洗数据
        String autoSearchDataSql = marketingDataFileConfig.getAutoSearchDataSql();
        String apiCode = marketingDataFileConfig.getApiCode();
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(5, 5);
        while (true) {
            List<Map<String, Object>> cleanDataMapList = marketingDataFileConfigMapper.selectCleanData(autoSearchDataSql);
            if (cleanDataMapList.size() == 0) {
                break;
            }
            String tasId = apiCode.concat("_").concat(yyyyMMdd);
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
                processUploadCleanData(cleanDataMapList, fieldVos, apiCode, tasId, pushPool,collect,marketingDataFileConfig);
            }
            // 转化
            if (cleanType == 1) {
                processTransferCleanData(cleanDataMapList, fieldVos, apiCode, pushPool,collect,marketingDataFileConfig);
            }
        }
        pushPool.shutdown();
        while (true) {
            try {
                if (!!pushPool.awaitTermination(5L, TimeUnit.SECONDS)) break;
            } catch (InterruptedException e) {
                log.error("线程池终止", e);
                Thread.currentThread().interrupt();
            }

        }


    }

    private void processTransferCleanData(List<Map<String, Object>> cleanDataMapList, List<FileToMarketingFieldVO> fieldVos,
                                          String apiCode, ThreadPoolExecutor pushPool,Set<Object> collect,
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

            }
        }
        asyncTransferData(transferDataItemDTOS, apiCode, pushPool,collect,marketingDataFileConfig);
    }

    private void processUploadCleanData(List<Map<String, Object>> cleanDataMapList, List<FileToMarketingFieldVO> fieldVos,
                                        String apiCode, String tasId, ThreadPoolExecutor pushPool,Set<Object> collect,
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

            }
        }
        asyncUploadDataNew(apiCode, tasId, pushPool, marketingPreUserDetailDTOS,collect,marketingDataFileConfig);
    }

    private <T> void cleanData(List<FileToMarketingFieldVO> fieldVos,
                               Map<String, Object> cleanDataMap, T o,
                               JSONObject reserveFieldJo) throws IOException, IllegalAccessException {
        Field[] declaredFields = o.getClass().getDeclaredFields();
        for (int d = 0; d < declaredFields.length; d++) {
            Field field = declaredFields[d];
            for (int j = 0; j < fieldVos.size(); j++) {
                FileToMarketingFieldVO fileToMarketingFieldVO = fieldVos.get(j);
                String interfaceField = fileToMarketingFieldVO.getInterfaceField();
                String headField = fileToMarketingFieldVO.getHeadField();
                String defaultValue = fileToMarketingFieldVO.getDefaultValue();
                Boolean isExtend = fileToMarketingFieldVO.getIsExtend();
                String conversion = fileToMarketingFieldVO.getConversion();
                Boolean isDateTransform = fileToMarketingFieldVO.getIsDateTransform();
                if (field.getName().equals(interfaceField)) {
                    Object dbValue = cleanDataMap.get(headField);
                    field.setAccessible(true);
                    Object fieldValue;
                    // 处理默认值
                    if (StringUtils.isNotBlank(defaultValue)) {
                        fieldValue = defaultValue;
                    } else {
                        fieldValue = dbValue;
                    }
                    // 时间格式转换
                    if (isDateTransform) {
                        fieldValue = getFormatterValue(String.valueOf(fieldValue));
                    }
                    // 处理字段转换 男 - > 1 女 -> 2
                    if (StringUtils.isNotBlank(conversion)) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        List<Map<String, String>> genderMappings = objectMapper.readValue(conversion, List.class);
                        if (!genderMappings.isEmpty()) {
                            Map<String, String> genderMapping = genderMappings.get(0);
                            fieldValue = genderMapping.get(fieldValue);
                        }
                    }
                    field.set(o, dbValue);
                    // 扩展字段容器
                    if (isExtend) {
                        reserveFieldJo.put(field.getName(), fieldValue);
                    }
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
        }
    }


    /**
     * 异步调用上传数据接口
     *
     * @param apiCode   apiCode
     * @param tasId     tasId
     * @param pushPool  上传使用线程池
     * @param syncUsers 具体数据对象
     */
    private void asyncUploadDataNew(String apiCode, String tasId
            , ThreadPoolExecutor pushPool, List<MarketingPreUserDetailDTO> syncUsers
    ,Set<Object> collect,MarketingDataFileConfig marketingDataFileConfig) {
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(tasId);
        marketingPreUserDTO.setRequestId(tasId);
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        pushPool.submit(() -> {
            Result result = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
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
        }else {
            marketingDataFileConfigMapper.updateCleanDataStatus(
                    marketingDataFileConfig.getAutoTableName(),
                    "3",
                    marketingDataFileConfig.getAutoDuplicateColumn(),
                    collect);
        }
    }

    /**
     * 异步调用转化数据接口
     *
     * @param apiCode  apiCode
     * @param pushPool 上传使用线程池
     */

    private void asyncTransferData(List<TransferDataItemDTO> transferDataItemDTOS, String apiCode,
                                   ThreadPoolExecutor pushPool,Set<Object> collect,
                                   MarketingDataFileConfig marketingDataFileConfig) {
        // 数据清洗
        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(transferDataItemDTOS);
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String taskId = yyyyMMdd.concat("_").concat(apiCode);
        String requestId = taskId.concat("_").concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();
        transferDataDTO.setRequestId(requestId);
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        pushPool.submit(() -> {
            Result result = pushInfoService.pushTransferByRetry(dto, null);
            updateStatus(collect, marketingDataFileConfig, result);
        });
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
                    log.error("无法解析日期;格式:{};原值:{}", parser, value);
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
