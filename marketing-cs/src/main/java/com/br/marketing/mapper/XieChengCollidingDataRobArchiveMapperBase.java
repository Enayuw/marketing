package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataRobArchive;
import com.br.marketing.entity.XieChengCollidingDataRobArchiveExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataRobArchiveMapperBase {
    int countByExample(XieChengCollidingDataRobArchiveExample example);

    int deleteByExample(XieChengCollidingDataRobArchiveExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataRobArchive record);

    int insertSelective(XieChengCollidingDataRobArchive record);

    List<XieChengCollidingDataRobArchive> selectByExampleWithBLOBs(XieChengCollidingDataRobArchiveExample example);

    List<XieChengCollidingDataRobArchive> selectByExample(XieChengCollidingDataRobArchiveExample example);

    XieChengCollidingDataRobArchive selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataRobArchive record, @Param("example") XieChengCollidingDataRobArchiveExample example);

    int updateByExampleWithBLOBs(@Param("record") XieChengCollidingDataRobArchive record, @Param("example") XieChengCollidingDataRobArchiveExample example);

    int updateByExample(@Param("record") XieChengCollidingDataRobArchive record, @Param("example") XieChengCollidingDataRobArchiveExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataRobArchive record);

    int updateByPrimaryKeyWithBLOBs(XieChengCollidingDataRobArchive record);

    int updateByPrimaryKey(XieChengCollidingDataRobArchive record);
}