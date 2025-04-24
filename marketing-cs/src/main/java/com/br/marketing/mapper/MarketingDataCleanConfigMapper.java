package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataCleanConfig;

import java.util.List;

public interface MarketingDataCleanConfigMapper extends MarketingDataCleanConfigMapperBase{

    List<MarketingDataCleanConfig> selectConfigs(String apiCode, Integer cleanType, String bizAction);

}