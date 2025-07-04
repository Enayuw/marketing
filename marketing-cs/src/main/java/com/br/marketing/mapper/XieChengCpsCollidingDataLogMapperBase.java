package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCpsCollidingDataLog;
import com.br.marketing.entity.XieChengCpsCollidingDataLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCpsCollidingDataLogMapperBase {
    int countByExample(XieChengCpsCollidingDataLogExample example);

    int deleteByExample(XieChengCpsCollidingDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCpsCollidingDataLog record);

    int insertSelective(XieChengCpsCollidingDataLog record);

    List<XieChengCpsCollidingDataLog> selectByExample(XieChengCpsCollidingDataLogExample example);

    XieChengCpsCollidingDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCpsCollidingDataLog record, @Param("example") XieChengCpsCollidingDataLogExample example);

    int updateByExample(@Param("record") XieChengCpsCollidingDataLog record, @Param("example") XieChengCpsCollidingDataLogExample example);

    int updateByPrimaryKeySelective(XieChengCpsCollidingDataLog record);

    int updateByPrimaryKey(XieChengCpsCollidingDataLog record);
}