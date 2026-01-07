package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MarketingTcyrCustCellMappingMapper extends MarketingTcyrCustCellMappingMapperBase{

    String selectNumUserKeyCellBytikv_(@Param("userKey")String userKey);

    String selectStrUserKeyCellBytikv_(@Param("userKey")String userKey);

    List<Map<String, Object>> selectCellInfotikv_(@Param("apiCode")String apiCode,@Param("idList") List<Long> idList);

    void saveNewCustCellInfo(@Param("userKeyId")Long userKeyId, @Param("cell")String cell);

    void batchSaveCustCell(@Param("insertSql") StringBuilder insertSql);

    void saveStrCustCellInfo(@Param("userKey") String userKey, @Param("cell") String cell);

    String selectCellBySyncId(@Param("apiCode")String apiCode,@Param("id") Long id);
}