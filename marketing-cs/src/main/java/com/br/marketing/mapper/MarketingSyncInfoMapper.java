package com.br.marketing.mapper;


import com.br.marketing.entity.MarketingSyncInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSyncInfoMapper extends MarketingSyncInfoMapperBase {
    List<String> getCusBatchByApiAndTime(@Param("apiCode") String apiCode,@Param("beginTime") String beginTime,@Param("endTime") String endTime);

    Long minSyncId(@Param("apiCode") String apiCode,@Param("cusBatch")String cusBatch,@Param("beginTime") String beginTime,@Param("endTime") String endTime);

    List<MarketingSyncInfo> getDatalimit(@Param("apiCode") String apiCode,@Param("cusBatch")String cusBatch
                                         ,@Param("id") Long id,@Param("beginTime") String beginTime
                                         ,@Param("endTime") String endTime);

    void createMarketingTransferTable(@Param("tableName") String tableName);

    Integer insertBatchTransfer(@Param("execSql") String execSql);

    Integer selectTransfersByRequestId(@Param("apiCode") String apiCode,@Param("requestId") String requestId);
}