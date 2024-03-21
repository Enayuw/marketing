package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataLogMapper extends XieChengCollidingDataLogMapperBase{
    List<XieChengCollidingDataLog> selectDeleteData(@Param("startTime") String startTime, @Param("size")  int size);
    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size")  int size);
}