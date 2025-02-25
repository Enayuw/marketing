package com.br.marketing.mapper;

import com.br.marketing.entity.DrsCustomizeUploadData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DrsCustomizeUploadDataMapper {

    void createDrsCustomizeUploadDataTable(@Param("tCid") String tCid);

    int insertSelective(DrsCustomizeUploadData record);

    DrsCustomizeUploadData selectById(@Param("tCid") String tCid, @Param("sourceId") String sourceId);

    void updateSyncStatusById(@Param("tCid") String tCid, @Param("sourceId") String sourceId, @Param("syncStatus") int syncStatus);

    void updateSyncStatusByIds(@Param("tCid") String tCid, @Param("sourceIds") List<Long> sourceIds, @Param("syncStatus") int syncStatus);

    List<DrsCustomizeUploadData> getDataOfNeedClean(@Param("tCid") String tCid
            , @Param("apiCodes") List<String> apiCodes
            , @Param("receiveDates") List<String> receiveDates
            , @Param("pageSize") Integer pageSize);

    List<DrsCustomizeUploadData> getDataOfToBeSync(@Param("tCid") String tcId, @Param("apiCodes") List<String> apiCodes, @Param("receiveDates")
            List<String> receiveDates, @Param("pageSize") Integer pageSize, @Param("indexId") Long indexId);


    void updateExtendAndStatusById(@Param("tCid") String tCid, @Param("id") Long id,@Param("syncStatus") int syncStatus, @Param("extend") String extend);
}
