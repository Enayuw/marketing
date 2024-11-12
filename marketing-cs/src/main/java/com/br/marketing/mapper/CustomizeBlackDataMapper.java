package com.br.marketing.mapper;

import com.br.marketing.entity.CustomizeBlackData;
import com.br.marketing.entity.CustomizeUploadData;
import org.apache.ibatis.annotations.Param;

public interface CustomizeBlackDataMapper {

    void createCustomizeBlackDataTable(@Param("tCid") String tCid);

    int insertSelective(CustomizeBlackData record);

    CustomizeBlackData selectById(@Param("tCid")String tCid, @Param("sourceId")String sourceId);

    void updateSyncStatusById(@Param("tCid") String tCid, @Param("sourceId") String sourceId, @Param("syncStatus") int syncStatus);
}
