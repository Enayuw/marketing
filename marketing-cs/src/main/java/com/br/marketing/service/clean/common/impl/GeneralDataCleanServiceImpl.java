package com.br.marketing.service.clean.common.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.FlagData;
import com.br.marketing.entity.MarketingDataCleanConfig;
import com.br.marketing.mapper.MarketingDataCleanConfigMapper;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import groovy.util.logging.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeneralDataCleanServiceImpl implements GeneralDataCleanService {

    @Resource
    private MarketingDataCleanConfigMapper marketingDataCleanConfigMapper;

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
    public Result uploadClean(List<JSONObject> data, String apiCode) throws NoSuchFieldException {
        return this.uploadClean(data, apiCode, BIZ_ACTION);
    }

    @Override
    public Result transferClean(List<JSONObject> data, String apiCode) {
        return this.transferClean(data, apiCode, BIZ_ACTION);
    }

    @Override
    public Result uploadClean(List<JSONObject> data, String apiCode, String bizAction) throws NoSuchFieldException {
        //1.查询清洗配置
        List<MarketingDataCleanConfig> configs = marketingDataCleanConfigMapper.selectConfigs(apiCode, CLEAN_TYPE_UPLOAD, bizAction);
        if (CollectionUtils.isEmpty(configs)) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("未查询到清洗配置");
        }
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
        //3.配置按targetName分组
        Map<String, List<MarketingDataCleanConfig>> configsGroup = configs.stream()
                .collect(Collectors.groupingBy(MarketingDataCleanConfig::getTargetName));
        List<MarketingPreUserDetailDTO> marketingPreUserDetailDTOS = new ArrayList<>();
        data.parallelStream().forEach(datum -> {
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
            JSONObject extend = extendStandardizat(datum);
            for (String targetName : configsGroup.keySet()) {
                Object fieldValue = null;
                List<MarketingDataCleanConfig> configList = configsGroup.get(targetName);
                MarketingDataCleanConfig configExample = configList.get(0);
                if(configExample.getMappingMode() == MAPPING_MODE_MAPPING){
                    //取值
                    if (configExample.getOriginType() == ORIGIN_TYPE_BASIC) {
                        fieldValue = datum.get(configExample.getOriginName());
                    } else if (configExample.getOriginType() == ORIGIN_TYPE_EXTEND) {
                        fieldValue = extend.get(configExample.getOriginName());
                    }
                    fieldFormat(fieldValue, configExample);
                    //赋值
                    if(configExample.getTargetType() == TARGET_TYPE_BASIC){
                        Field field = fieldMap.get(configExample.getTargetName());
                    }

                }
            }
        });
        return null;
    }

    /**
     * @description 格式化
     * @param fieldValue
     * @param config
     * @return void
     * @author hedongshuo
     * @date 2025/4/24 17:15
     **/
    private void fieldFormat(Object fieldValue, MarketingDataCleanConfig config) {
        if (StringUtils.isNotBlank(config.getConversion())) {
            JSONObject.parseObject(config.getConversion());
        }
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

    @Override
    public Result transferClean(List<JSONObject> data, String apiCode, String bizAction) {
        return null;
    }
}
