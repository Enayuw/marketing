package com.br.marketing.mapper;

import com.br.marketing.entity.RetryMainLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RetryMainLogMapper extends RetryMainLogMapperBase {

    Long getMinIdByNeedRetryData();

    List<RetryMainLog> getNeedRetryData(@Param("minId") Long minId);
}