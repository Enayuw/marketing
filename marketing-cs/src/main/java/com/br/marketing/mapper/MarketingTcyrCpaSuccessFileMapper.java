package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSuccessFile;
import org.apache.ibatis.annotations.Param;

public interface MarketingTcyrCpaSuccessFileMapper extends MarketingTcyrCpaSuccessFileMapperBase{

    MarketingTcyrCpaSuccessFile selectNoDealSingleSyncFile(
            @Param("apiCode") String apiCode,
            @Param("syncDataDealStatus") Integer syncDataDealStatus);

    void updateSyncDataDealStatus(
            @Param("id") Long id,
            @Param("syncDataDealStatus") Integer syncDataDealStatus);

    void updateSyncDealStatusAndSuccesCount(
            @Param("id") Long id,
            @Param("syncDataDealStatus") Integer syncDataDealStatus,
            @Param("addSuccessCount") Long addSuccessCount);
}