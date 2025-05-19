package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataDelayLoopCycle;
import com.br.marketing.entity.WubaCollidingDataDelayLoopCycleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataDelayLoopCycleMapperBase {
    int countByExample(WubaCollidingDataDelayLoopCycleExample example);

    int deleteByExample(WubaCollidingDataDelayLoopCycleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaCollidingDataDelayLoopCycle record);

    int insertSelective(WubaCollidingDataDelayLoopCycle record);

    List<WubaCollidingDataDelayLoopCycle> selectByExample(WubaCollidingDataDelayLoopCycleExample example);

    WubaCollidingDataDelayLoopCycle selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaCollidingDataDelayLoopCycle record,
                                 @Param("example") WubaCollidingDataDelayLoopCycleExample example);

    int updateByExample(@Param("record") WubaCollidingDataDelayLoopCycle record, @Param("example") WubaCollidingDataDelayLoopCycleExample example);

    int updateByPrimaryKeySelective(WubaCollidingDataDelayLoopCycle record);

    int updateByPrimaryKey(WubaCollidingDataDelayLoopCycle record);
}