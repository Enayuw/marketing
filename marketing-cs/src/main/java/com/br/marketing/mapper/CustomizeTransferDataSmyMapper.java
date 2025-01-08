package com.br.marketing.mapper;

import com.br.marketing.entity.CustomizeTransferDataSmy;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomizeTransferDataSmyMapper {
    void createCustomizeTransferDataTable(@Param("tCid") String tCid);

    int insertSelective(CustomizeTransferDataSmy customizeTransferDataSmy);

    Long smyCleanCustomizedUploadDataOfMinId(@Param("tCid") String tCid, @Param("apiCode") String apiCode, @Param("date") String date);

    List<CustomizeTransferDataSmy> smyCleanCustomizedUploadDataByMinId(@Param("tCid") String tCid, @Param("apiCode") String apiCode,
                                                                       @Param("date") String date, @Param("minId") Long minId,
                                                                       @Param("limit") Integer limit);

    void updateSyncStatusByIds(@Param("tCid") String tCid, @Param("ids") List<Long> ids, @Param("syncStatus") int syncStatus);

    void updateSyncStatusById(@Param("tCid") String tCid, @Param("id") Long id, @Param("syncStatus") int syncStatus);
}