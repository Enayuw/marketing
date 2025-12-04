package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TcyrCpaDeleteRuleMapper extends TcyrCpaDeleteRuleMapperBase{

    Integer calculateDeleteNumByScript(@Param("executeScript") String executeScript);

    Integer executeUnionQueries(@Param("scripts") List<String> scripts);

}