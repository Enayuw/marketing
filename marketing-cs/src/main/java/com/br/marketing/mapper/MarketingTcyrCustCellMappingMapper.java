package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MarketingTcyrCustCellMappingMapper extends MarketingTcyrCustCellMappingMapperBase{

    String selectCellBySyncId(@Param("apiCode")String apiCde,@Param("id") Long id);

    List<Map<String, Object>> selectCellInfotikv_(@Param("apiCode") String apiCode,@Param("idList") List<Long> idList);
}