package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaCommonMapper {

    Integer calculateDeleteNumByScript(@Param("executeScript") String executeScript);

    Integer executeUnionQueriestikv_(@Param("scripts") List<String> scripts);

    Integer magnitudeQuerytiflash_(@Param("querySql") String querySql);

}