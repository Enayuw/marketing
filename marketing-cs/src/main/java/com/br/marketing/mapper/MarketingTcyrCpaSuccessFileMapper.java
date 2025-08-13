package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSuccessFile;
import org.apache.ibatis.annotations.Param;

public interface MarketingTcyrCpaSuccessFileMapper extends MarketingTcyrCpaSuccessFileMapperBase{

    MarketingTcyrCpaSuccessFile selectSyncNoDealSingleFile(
            @Param("apiCode") String apiCode,
            @Param("syncDataDealStatus") Integer syncDataDealStatus);

    MarketingTcyrCpaSuccessFile selectColliDingNoDealSingleFile(
            @Param("apiCode") String apiCode,
            @Param("collidingDataDealStatus") Integer collidingDataDealStatus);

    void updateSyncDataDealStatus(
            @Param("id") Long id,
            @Param("syncDataDealStatus") Integer syncDataDealStatus);

    void updateSyncDealStatusAndSuccesCount(
            @Param("id") Long id,
            @Param("syncDataDealStatus") Integer syncDataDealStatus,
            @Param("addSuccessCount") Long addSuccessCount);

    void updateColliDingDataDealStatus(
            @Param("id") Long id,
            @Param("collidingDataDealStatus") Integer collidingDataDealStatus);

    void updateColliDingDataDealStatusAndTotalCount(
            @Param("id") Long id,
            @Param("collidingDataDealStatus") Integer collidingDataDealStatus,
            @Param("addTotalCount")Long addTotalCount);

    MarketingTcyrCpaSuccessFile selectFileByFilePath(
            @Param("apiCode") String apiCode,
            @Param("filePath") String filePath);
}