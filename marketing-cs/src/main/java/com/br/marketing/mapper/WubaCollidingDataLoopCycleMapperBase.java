package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataLoopCycle;
import com.br.marketing.entity.WubaCollidingDataLoopCycleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataLoopCycleMapperBase {
    int countByExample(WubaCollidingDataLoopCycleExample example);

    int deleteByExample(WubaCollidingDataLoopCycleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaCollidingDataLoopCycle record);

    int insertSelective(WubaCollidingDataLoopCycle record);

    List<WubaCollidingDataLoopCycle> selectByExample(WubaCollidingDataLoopCycleExample example);

    WubaCollidingDataLoopCycle selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaCollidingDataLoopCycle record, @Param("example") WubaCollidingDataLoopCycleExample example);

    int updateByExample(@Param("record") WubaCollidingDataLoopCycle record, @Param("example") WubaCollidingDataLoopCycleExample example);

    int updateByPrimaryKeySelective(WubaCollidingDataLoopCycle record);

    int updateByPrimaryKey(WubaCollidingDataLoopCycle record);
}