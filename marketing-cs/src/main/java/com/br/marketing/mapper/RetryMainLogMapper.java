package com.br.marketing.mapper;

import com.br.marketing.entity.RetryMainLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RetryMainLogMapper extends RetryMainLogMapperBase {

    Long getMinIdByNeedRetryData();

    List<RetryMainLog> getNeedRetryData(@Param("minId") Long minId);

    Long getMinIdByNeedRetryWithMethodData(@Param("service") String service,@Param("method") String method);

    List<RetryMainLog> getNeedRetryWithMethodData(@Param("minId") Long minId,@Param("service") String service,@Param("method") String method,@Param("pageSize") Integer pageSize);
}