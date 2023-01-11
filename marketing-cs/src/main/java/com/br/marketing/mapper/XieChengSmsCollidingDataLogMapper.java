package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengData;
import com.br.marketing.entity.XieChengSmsCollidingData;
import com.br.marketing.entity.XieChengSmsCollidingDataLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengSmsCollidingDataLogMapper extends XieChengSmsCollidingDataLogMapperBase{


   XieChengSmsCollidingDataLog selectByCodeAndTime(@Param("sha256CodeList") String sha256CodeList, @Param("lastTimeDay") String lastTimeDay);
}