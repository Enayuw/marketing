package com.br.marketing.mapper;


import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ErrorMarkMapper extends ErrorMarkMapperBase {

    List<Integer> queryRetryTotalAttempts(@Param("id") Long id,
                                          @Param("retryStatus") Integer retryStatus,
                                          @Param("filterType") Integer filterType);
}