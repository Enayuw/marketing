package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.CustomizeUploadData;

public interface CustomizeUploadDataMapper {

    void createCustomizeUploadDataTable(@Param("tCid") String tCid);

    int insertSelective(CustomizeUploadData record);

    CustomizeUploadData selectById(@Param("tCid")String tCid, @Param("sourceId")String sourceId);

    void updateSyncStatusById(@Param("tCid") String tCid, @Param("sourceId") String sourceId, @Param("syncStatus") int syncStatus);
}
