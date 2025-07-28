package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCpsCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCpsCollidingDataLoopCycleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCpsCollidingDataLoopCycleMapperBase {
    int countByExample(XieChengCpsCollidingDataLoopCycleExample example);

    int deleteByExample(XieChengCpsCollidingDataLoopCycleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCpsCollidingDataLoopCycle record);

    int insertSelective(XieChengCpsCollidingDataLoopCycle record);

    List<XieChengCpsCollidingDataLoopCycle> selectByExample(XieChengCpsCollidingDataLoopCycleExample example);

    XieChengCpsCollidingDataLoopCycle selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCpsCollidingDataLoopCycle record, @Param("example") XieChengCpsCollidingDataLoopCycleExample example);

    int updateByExample(@Param("record") XieChengCpsCollidingDataLoopCycle record, @Param("example") XieChengCpsCollidingDataLoopCycleExample example);

    int updateByPrimaryKeySelective(XieChengCpsCollidingDataLoopCycle record);

    int updateByPrimaryKey(XieChengCpsCollidingDataLoopCycle record);
}