package com.br.marketing.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MarketingRuleCenterHaloCallbackDataMapper extends MarketingRuleCenterHaloCallbackDataMapperBase {

    @MapKey("id")
    List<Map<String, Object>> selectByTaskIdAndBatchNumber(@Param("apiCode") String apiCode, @Param("taskId") Long taskId, @Param("minId") Long minId, @Param("pageSize") Integer pageSize, @Param("status") Integer status);

    void updateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

}
