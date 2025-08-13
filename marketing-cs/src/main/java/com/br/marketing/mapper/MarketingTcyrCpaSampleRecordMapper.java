package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSampleRecord;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface MarketingTcyrCpaSampleRecordMapper extends MarketingTcyrCpaSampleRecordMapperBase{
    List<MarketingTcyrCpaSampleRecord> searchTcyrSyncList(@Param("apiCode")String apiCode, @Param("status")Integer status, @Param("dayBeginTime") Date dayBeginTime, @Param("dayEndTime") Date dayEndTime);

    Integer updageTcyrSampleRecordDownStatus(@Param("batchNo") String batchNo, @Param("downStatus") Integer downStatus);
}