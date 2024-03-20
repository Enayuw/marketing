package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataContrast;
import com.br.marketing.entity.XieChengCollidingDataContrastExample;
import com.br.marketing.entity.XieChengCollidingDataRob;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataContrastMapper extends XieChengCollidingDataContrastMapperBase{
    List<XieChengCollidingDataContrast> selectDeleteData(@Param("startTime") String startTime, @Param("size")  int size);
    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size")  int size);
}