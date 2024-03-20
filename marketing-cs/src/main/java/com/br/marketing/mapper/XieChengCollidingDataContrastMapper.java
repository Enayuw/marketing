package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataContrast;
import com.br.marketing.entity.XieChengCollidingDataContrastExample;
import com.br.marketing.entity.XieChengCollidingDataRob;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface XieChengCollidingDataContrastMapper extends XieChengCollidingDataContrastMapperBase{
    List<XieChengCollidingDataContrast> selectDeleteData(@Param("startTime") String startTime, @Param("size")  int size);
    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size")  int size);
    Long temporaryCounttiflash_(@Param("tableName") String tableName,@Param("filterScore") String filterScore);
    Map<String,Long> temporaryMaxAndMinId(@Param("tableName") String tableName,@Param("filterScore") String filterScore);
    List<Map<String, String>> temporaryCelltiflash_(@Param("tableName") String tableName,@Param("filterScore")
    String filterScore,@Param("xieChengCleanLimitCount") Integer xieChengCleanLimitCount);
    int saveBatch(List<XieChengCollidingDataContrast> xieChengCollidingDataContrastList);
}