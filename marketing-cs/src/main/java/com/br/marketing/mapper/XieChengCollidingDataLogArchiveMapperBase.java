package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataLogArchive;
import com.br.marketing.entity.XieChengCollidingDataLogArchiveExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataLogArchiveMapperBase {
    int countByExample(XieChengCollidingDataLogArchiveExample example);

    int deleteByExample(XieChengCollidingDataLogArchiveExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataLogArchive record);

    int insertSelective(XieChengCollidingDataLogArchive record);

    List<XieChengCollidingDataLogArchive> selectByExample(XieChengCollidingDataLogArchiveExample example);

    XieChengCollidingDataLogArchive selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataLogArchive record, @Param("example") XieChengCollidingDataLogArchiveExample example);

    int updateByExample(@Param("record") XieChengCollidingDataLogArchive record, @Param("example") XieChengCollidingDataLogArchiveExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataLogArchive record);

    int updateByPrimaryKey(XieChengCollidingDataLogArchive record);
}