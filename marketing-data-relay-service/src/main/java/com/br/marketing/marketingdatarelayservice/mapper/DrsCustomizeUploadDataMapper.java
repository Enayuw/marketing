package com.br.marketing.marketingdatarelayservice.mapper;

import com.br.marketing.marketingdatarelayservice.entity.DrsCustomizeUploadData;
import org.apache.ibatis.annotations.Param;

public interface DrsCustomizeUploadDataMapper {

    void createDrsCustomizeUploadDataTable(@Param("tCid") String tCid);

    int insertSelective(DrsCustomizeUploadData record);

    DrsCustomizeUploadData selectById(@Param("tCid")String tCid, @Param("sourceId")String sourceId);

    void updateSyncStatusById(@Param("tCid") String tCid, @Param("sourceId") String sourceId, @Param("syncStatus") int syncStatus);
}
