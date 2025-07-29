package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrSyncFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrSyncFileMapper extends MarketingTcyrSyncFileMapperBase {

    MarketingTcyrSyncFile selectSingleSyncFile(
            @Param("apiCode") String apiCode,
            @Param("dealStatus") Integer dealStatus
    );


    List<MarketingTcyrSyncFile> selectSyncFileList(
            @Param("apiCode") String apiCode,
            @Param("dealStatus") Integer dealStatus);


    MarketingTcyrSyncFile selectNoDealSingleSyncFile(
            @Param("apiCode") String apiCode,
            @Param("quickDealStatus") Integer quickDealStatus,
            @Param("dbDealStatus") Integer dbDealStatus
    );

    void updateQuickDealStatus(
            @Param("id") Long id,
            @Param("quickDealStatus") Integer quickDealStatus);

    void updateDbDealStatus(
            @Param("id") Long id,
            @Param("dbDealStatus") Integer dbDealStatus);

    void updateSuccessCount(
            @Param("syncFileId") Long syncFileId,
            @Param("addSuccessCount") Integer addSuccessCount);

    void updateQuickDealAndSuccesCount(
            @Param("syncFileId") Long syncFileId,
            @Param("quickDealStatus") Integer quickDealStatus,
            @Param("addSuccessCount") Long addSuccessCount);

}