package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MarketingTcyrCustCellMappingMapper extends MarketingTcyrCustCellMappingMapperBase{

    String selectCelltikv_(@Param("userKeyId")Long userKeyId);

    void saveNewCustCellInfo(@Param("userKey")String userKey, @Param("cell")String cell);

    void batchSaveCustCell(@Param("insertSql") StringBuilder insertSql);

    List<Map<String, String>> selectCellInfotiflash_(@Param("userKeyList") List<String> userKeyList);

    List<Map<String, Object>> selectCellInfotikv_(@Param("userKeyList") List<Long> userKeyList);

}