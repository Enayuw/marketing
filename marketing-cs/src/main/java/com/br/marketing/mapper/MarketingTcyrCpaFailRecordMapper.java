package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaFailRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaFailRecordMapper extends MarketingTcyrCpaFailRecordMapperBase {

    List<MarketingTcyrCpaFailRecord> searchTcyrFailRecordList(@Param("apiCode") String apiCode,
                                                              @Param("status") Integer status);

    void updateTcyrRecordDownStatus(@Param("id") Long id, @Param("downStatus") Integer downStatus);
}