package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataCleanConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDataCleanConfigMapper extends MarketingDataCleanConfigMapperBase{

    List<MarketingDataCleanConfig> selectConfigs(
            @Param("apiCode")String apiCode,
            @Param("cleanType")Integer cleanType,
            @Param("bizAction")String bizAction);

}