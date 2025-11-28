package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface TcyrCpaDeleteRuleMapper extends TcyrCpaDeleteRuleMapperBase{

    Integer calculateDeleteNumByScript(@Param("executeScript") String executeScript);

}