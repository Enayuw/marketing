package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengData;
import com.br.marketing.entity.XieChengSmsCollidingData;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface XieChengSmsCollidingDataMapper extends XieChengSmsCollidingDataMapperBase{



    List<XieChengSmsCollidingData> selectByLocalId(@Param("localId") Long localId, @Param("minId") Long minId,@Param("endTime") String endTime);

}