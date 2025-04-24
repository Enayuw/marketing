package com.br.marketing.service.clean.common.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.MarketingDataCleanConfig;
import com.br.marketing.mapper.MarketingDataCleanConfigMapper;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import groovy.util.logging.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeneralDataCleanServiceImpl implements GeneralDataCleanService {

    @Resource
    private MarketingDataCleanConfigMapper marketingDataCleanConfigMapper;

    private static final Integer CLEAN_TYPE_UPLOAD = 0;

    private static final Integer CLEAN_TYPE_TRANSFER = 1;
    private static final String BIZ_ACTION = "common";

    @Override
    public Result uploadClean(List<JSONObject> data, String apiCode) {
        return this.uploadClean(data, apiCode, BIZ_ACTION);
    }

    @Override
    public Result transferClean(List<JSONObject> data, String apiCode) {
        return this.transferClean(data, apiCode, BIZ_ACTION);
    }

    @Override
    public Result uploadClean(List<JSONObject> data, String apiCode, String bizAction) {
        //1.查询清洗配置
        List<MarketingDataCleanConfig> configs = marketingDataCleanConfigMapper.selectConfigs(apiCode, CLEAN_TYPE_UPLOAD, bizAction);
        if (CollectionUtils.isEmpty(configs)) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("未查询到清洗配置");
        }
        Map<String, List<MarketingDataCleanConfig>> configsGroup =
                configs.stream().collect(Collectors.groupingBy(MarketingDataCleanConfig::getTargetName));

        return null;
    }

    @Override
    public Result transferClean(List<JSONObject> data, String apiCode, String bizAction) {
        return null;
    }
}
