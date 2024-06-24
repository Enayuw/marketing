package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCollidingDataLoopCycleExample;

public interface XieChengCollidingDataLoopCycleMapperBase {
    int countByExample(XieChengCollidingDataLoopCycleExample example);

    int deleteByExample(XieChengCollidingDataLoopCycleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataLoopCycle record);

    int insertSelective(XieChengCollidingDataLoopCycle record);

    List<XieChengCollidingDataLoopCycle> selectByExample(XieChengCollidingDataLoopCycleExample example);

    XieChengCollidingDataLoopCycle selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataLoopCycle record,
        @Param("example") XieChengCollidingDataLoopCycleExample example);

    int updateByExample(@Param("record") XieChengCollidingDataLoopCycle record, @Param("example") XieChengCollidingDataLoopCycleExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataLoopCycle record);

    int updateByPrimaryKey(XieChengCollidingDataLoopCycle record);
}