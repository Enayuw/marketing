package com.br.marketing.mapper;

import com.br.marketing.dto.dewu.DewuPushQueryQuantityDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DewuCollidingDataMapper extends DewuCollidingDataMapperBase{

   int updateBatchById(@Param("ids") List<Long> ids,@Param("pushStatus")Integer pushStatus,@Param("pushDate")Integer pushDate);

   List<Map<String, Object>> queryQuantityGroupByLocalId(DewuPushQueryQuantityDTO params);

}