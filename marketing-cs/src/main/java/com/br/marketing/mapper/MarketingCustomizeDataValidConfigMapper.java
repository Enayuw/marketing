package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCustomizeDataValidConfig;
import com.br.marketing.entity.MarketingCustomizeDataValidConfigExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MarketingCustomizeDataValidConfigMapper extends MarketingCustomizeDataValidConfigMapperBase{
    Map<String,String> getCustomizeValidPeriodRangeByApiCodeAndUserType(@Param("id") Long id);
}