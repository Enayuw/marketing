package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DewuCollidingDataMapper extends DewuCollidingDataMapperBase{

   int updateBatchById(@Param("ids") List<Long> ids,@Param("pushStatus")Integer pushStatus,@Param("pushDate")Integer pushDate);

}