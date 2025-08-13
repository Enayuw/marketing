package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaFailRecord;
import com.br.marketing.entity.MarketingTcyrCpaFailRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaFailRecordMapper extends MarketingTcyrCpaFailRecordMapperBase{
    List<MarketingTcyrCpaFailRecord> searchTcyrFailRecordList(
            @Param("apiCode") String apiCode,
            @Param("status") Integer status,
            @Param("downStatus") Integer downStatus);

    void updateTcyrRecordDownStatus(
            @Param("id") Long id,
            @Param("downStatus") Integer downStatus);
}