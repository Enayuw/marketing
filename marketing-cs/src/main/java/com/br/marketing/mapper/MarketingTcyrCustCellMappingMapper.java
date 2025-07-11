package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface MarketingTcyrCustCellMappingMapper extends MarketingTcyrCustCellMappingMapperBase{

    String selectCelltikv_(@Param("userKey")String userKey);

    void saveNewCustCellInfo(@Param("userKey")String userKey, @Param("cell")String cell);

    void batchSaveCustCell(@Param("") StringBuilder insertSql);
}