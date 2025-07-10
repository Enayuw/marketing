package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface MarketingTcyrCustCellMappingMapper extends MarketingTcyrCustCellMappingMapperBase{

    String selectCelltiflash_(@Param("userKey") String userKey);
}