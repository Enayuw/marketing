package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaPushFileScriptMapper extends MarketingTcyrCpaPushFileScriptMapperBase{

    List<String> getTcyrCpaPushFileDatatikv_(@Param("extraSql") String querySql);

    List<String> getTcyrCpaPushFileDatabI_(@Param("extraSql") String querySql);

    Integer getTcyrCpaPushFileDataCounttikv_(@Param("extraCountSql") String querySql);

    Integer getTcyrCpaPushFileDataCountbI_(@Param("extraCountSql") String querySql);
}