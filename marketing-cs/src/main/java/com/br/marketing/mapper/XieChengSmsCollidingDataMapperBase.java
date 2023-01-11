package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengSmsCollidingData;
import com.br.marketing.entity.XieChengSmsCollidingDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengSmsCollidingDataMapperBase {
    int countByExample(XieChengSmsCollidingDataExample example);

    int deleteByExample(XieChengSmsCollidingDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengSmsCollidingData record);

    int insertSelective(XieChengSmsCollidingData record);

    List<XieChengSmsCollidingData> selectByExampleWithBLOBs(XieChengSmsCollidingDataExample example);

    List<XieChengSmsCollidingData> selectByExample(XieChengSmsCollidingDataExample example);

    XieChengSmsCollidingData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengSmsCollidingData record, @Param("example") XieChengSmsCollidingDataExample example);

    int updateByExampleWithBLOBs(@Param("record") XieChengSmsCollidingData record, @Param("example") XieChengSmsCollidingDataExample example);

    int updateByExample(@Param("record") XieChengSmsCollidingData record, @Param("example") XieChengSmsCollidingDataExample example);

    int updateByPrimaryKeySelective(XieChengSmsCollidingData record);

    int updateByPrimaryKeyWithBLOBs(XieChengSmsCollidingData record);

    int updateByPrimaryKey(XieChengSmsCollidingData record);
}