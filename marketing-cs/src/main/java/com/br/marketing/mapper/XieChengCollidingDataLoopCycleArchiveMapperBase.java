package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataLoopCycleArchive;
import com.br.marketing.entity.XieChengCollidingDataLoopCycleArchiveExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataLoopCycleArchiveMapperBase {
    int countByExample(XieChengCollidingDataLoopCycleArchiveExample example);

    int deleteByExample(XieChengCollidingDataLoopCycleArchiveExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataLoopCycleArchive record);

    int insertSelective(XieChengCollidingDataLoopCycleArchive record);

    List<XieChengCollidingDataLoopCycleArchive> selectByExampleWithBLOBs(XieChengCollidingDataLoopCycleArchiveExample example);

    List<XieChengCollidingDataLoopCycleArchive> selectByExample(XieChengCollidingDataLoopCycleArchiveExample example);

    XieChengCollidingDataLoopCycleArchive selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataLoopCycleArchive record, @Param("example") XieChengCollidingDataLoopCycleArchiveExample example);

    int updateByExampleWithBLOBs(@Param("record") XieChengCollidingDataLoopCycleArchive record, @Param("example") XieChengCollidingDataLoopCycleArchiveExample example);

    int updateByExample(@Param("record") XieChengCollidingDataLoopCycleArchive record, @Param("example") XieChengCollidingDataLoopCycleArchiveExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataLoopCycleArchive record);

    int updateByPrimaryKeyWithBLOBs(XieChengCollidingDataLoopCycleArchive record);

    int updateByPrimaryKey(XieChengCollidingDataLoopCycleArchive record);
}