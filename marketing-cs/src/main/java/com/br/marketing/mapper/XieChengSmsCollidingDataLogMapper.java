package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengSmsCollidingDataLogMapper extends XieChengSmsCollidingDataLogMapperBase{


   int selectByCodeAndTime(@Param("localId") Long localId,@Param("sha256CodeList") String sha256CodeList,@Param("lastTimeDay") String lastTimeDay);
}