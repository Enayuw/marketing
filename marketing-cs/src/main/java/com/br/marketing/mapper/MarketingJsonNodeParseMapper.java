package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MarketingJsonNodeParseMapper extends MarketingJsonNodeParseMapperBase{

    Map<String, Object> getOriginalData(@Param("id")Long id,@Param("tableName")String tableName);

}
