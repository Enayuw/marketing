package com.br.marketing.mapper;

import com.br.marketing.entity.DidiData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DidiDataMapper extends DidiDataMapperBase{

    List<DidiData> getFirstDidiByFiletikv_(@Param("localId") Long localId,@Param("cells") List<String> cells);

    Integer updateMarketingBatch(@Param("sql") String sql);

    List<String> getPushDateByLocalId(@Param("localId") Long localId);
}