package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSyncRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaSyncRecordMapper  extends MarketingTcyrCpaSyncRecordMapperBase {

    List<MarketingTcyrCpaSyncRecord> searchTcyrSyncRecordList(@Param("apiCode") String apiCode,
                                                              @Param("status") Integer status);

    void updateTcyrRecordDownStatus(@Param("id") Long id, @Param("downStatus") Integer downStatus);
}