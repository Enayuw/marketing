package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSuccessRecord;
import com.br.marketing.entity.MarketingTcyrCpaSuccessRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaSuccessRecordMapper extends MarketingTcyrCpaSuccessRecordMapperBase{
    List<MarketingTcyrCpaSuccessRecord> searchTcyrSyncRecordList(
            @Param("apiCode") String apiCode,
            @Param("status") Integer status,
            @Param("downStatus")Integer downStatus);

    void updateTcyrRecordDownStatus(
            @Param("id") Long id,
            @Param("downStatus") Integer downStatus);
}