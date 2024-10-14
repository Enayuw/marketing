package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataSecondLoopCycle;
import com.br.marketing.entity.WubaCollidingDataSecondLoopCycleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataSecondLoopCycleMapperBase {
    int countByExample(WubaCollidingDataSecondLoopCycleExample example);

    int deleteByExample(WubaCollidingDataSecondLoopCycleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaCollidingDataSecondLoopCycle record);

    int insertSelective(WubaCollidingDataSecondLoopCycle record);

    List<WubaCollidingDataSecondLoopCycle> selectByExample(WubaCollidingDataSecondLoopCycleExample example);

    WubaCollidingDataSecondLoopCycle selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaCollidingDataSecondLoopCycle record, @Param("example") WubaCollidingDataSecondLoopCycleExample example);

    int updateByExample(@Param("record") WubaCollidingDataSecondLoopCycle record, @Param("example") WubaCollidingDataSecondLoopCycleExample example);

    int updateByPrimaryKeySelective(WubaCollidingDataSecondLoopCycle record);

    int updateByPrimaryKey(WubaCollidingDataSecondLoopCycle record);
}