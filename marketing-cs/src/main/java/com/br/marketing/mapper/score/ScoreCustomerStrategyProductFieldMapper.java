package com.br.marketing.mapper.score;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScoreCustomerStrategyProductFieldMapper extends ScoreCustomerStrategyProductFieldMapperBase {

    List<String> getFieldNamePage(@Param("customerId") String customerId, @Param("offset") int offset, @Param("rowCount") int rowCount);

}