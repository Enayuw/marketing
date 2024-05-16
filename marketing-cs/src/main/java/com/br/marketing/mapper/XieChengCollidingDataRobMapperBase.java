package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.entity.XieChengCollidingDataRobExample;

public interface XieChengCollidingDataRobMapperBase {
    int countByExample(XieChengCollidingDataRobExample example);

    int deleteByExample(XieChengCollidingDataRobExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataRob record);

    int insertSelective(XieChengCollidingDataRob record);

    List<XieChengCollidingDataRob> selectByExample(XieChengCollidingDataRobExample example);

    XieChengCollidingDataRob selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataRob record, @Param("example") XieChengCollidingDataRobExample example);

    int updateByExample(@Param("record") XieChengCollidingDataRob record, @Param("example") XieChengCollidingDataRobExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataRob record);

    int updateByPrimaryKey(XieChengCollidingDataRob record);
}