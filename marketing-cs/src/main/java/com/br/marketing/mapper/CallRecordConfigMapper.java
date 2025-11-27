package com.br.marketing.mapper;


import org.apache.ibatis.annotations.Param;


public interface CallRecordConfigMapper extends CallRecordConfigMapperBase {

    String customerRuleLabels(@Param("apiCode") String apiCode);

}