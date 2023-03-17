package com.br.marketing.mapper;

import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface DataDistributeDetailLogMapper extends DataDistributeDetailLogMapperBase {

    Set<String> getToDataDistributeInfoList(@Param("apiCode") String apiCode, @Param("custNums") Set<String> custNums);
}