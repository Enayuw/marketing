package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengSmsCollidingDataLog;
import com.br.marketing.entity.XieChengSmsCollidingDataLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengSmsCollidingDataLogMapperBase {
    int countByExample(XieChengSmsCollidingDataLogExample example);

    int deleteByExample(XieChengSmsCollidingDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengSmsCollidingDataLog record);

    int insertSelective(XieChengSmsCollidingDataLog record);

    List<XieChengSmsCollidingDataLog> selectByExampleWithBLOBs(XieChengSmsCollidingDataLogExample example);

    List<XieChengSmsCollidingDataLog> selectByExample(XieChengSmsCollidingDataLogExample example);

    XieChengSmsCollidingDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengSmsCollidingDataLog record, @Param("example") XieChengSmsCollidingDataLogExample example);

    int updateByExampleWithBLOBs(@Param("record") XieChengSmsCollidingDataLog record, @Param("example") XieChengSmsCollidingDataLogExample example);

    int updateByExample(@Param("record") XieChengSmsCollidingDataLog record, @Param("example") XieChengSmsCollidingDataLogExample example);

    int updateByPrimaryKeySelective(XieChengSmsCollidingDataLog record);

    int updateByPrimaryKeyWithBLOBs(XieChengSmsCollidingDataLog record);

    int updateByPrimaryKey(XieChengSmsCollidingDataLog record);
}