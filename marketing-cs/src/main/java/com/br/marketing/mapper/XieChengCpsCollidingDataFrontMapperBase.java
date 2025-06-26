package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCpsCollidingDataFront;
import com.br.marketing.entity.XieChengCpsCollidingDataFrontExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCpsCollidingDataFrontMapperBase {
    int countByExample(XieChengCpsCollidingDataFrontExample example);

    int deleteByExample(XieChengCpsCollidingDataFrontExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCpsCollidingDataFront record);

    int insertSelective(XieChengCpsCollidingDataFront record);

    List<XieChengCpsCollidingDataFront> selectByExample(XieChengCpsCollidingDataFrontExample example);

    XieChengCpsCollidingDataFront selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCpsCollidingDataFront record, @Param("example") XieChengCpsCollidingDataFrontExample example);

    int updateByExample(@Param("record") XieChengCpsCollidingDataFront record, @Param("example") XieChengCpsCollidingDataFrontExample example);

    int updateByPrimaryKeySelective(XieChengCpsCollidingDataFront record);

    int updateByPrimaryKey(XieChengCpsCollidingDataFront record);
} 