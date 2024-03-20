package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.entity.XieChengCollidingDataLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataLogMapperBase {
    int countByExample(XieChengCollidingDataLogExample example);

    int deleteByExample(XieChengCollidingDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataLog record);

    int insertSelective(XieChengCollidingDataLog record);

    List<XieChengCollidingDataLog> selectByExample(XieChengCollidingDataLogExample example);

    XieChengCollidingDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataLog record, @Param("example") XieChengCollidingDataLogExample example);

    int updateByExample(@Param("record") XieChengCollidingDataLog record, @Param("example") XieChengCollidingDataLogExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataLog record);

    int updateByPrimaryKey(XieChengCollidingDataLog record);
}