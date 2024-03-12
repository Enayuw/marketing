package com.br.marketing.mapper;


import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface DataDistributeDetailLogMapper extends DataDistributeDetailLogMapperBase {

    Set<String> getToDataDistributeInfoList(@Param("apiCode") String apiCode, @Param("cells") Set<String> cells);
}