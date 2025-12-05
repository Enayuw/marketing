package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaCommonMapper {

    Integer calculateDeleteNumByScript(@Param("executeScript") String executeScript);

    Integer executeUnionQueries(@Param("scripts") List<String> scripts);

}