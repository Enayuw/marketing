package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaLoopCycle;
import com.br.marketing.entity.MarketingTcyrCpaLoopCycleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaLoopCycleMapperBase {
    int countByExample(MarketingTcyrCpaLoopCycleExample example);

    int deleteByExample(MarketingTcyrCpaLoopCycleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaLoopCycle record);

    int insertSelective(MarketingTcyrCpaLoopCycle record);

    List<MarketingTcyrCpaLoopCycle> selectByExample(MarketingTcyrCpaLoopCycleExample example);

    MarketingTcyrCpaLoopCycle selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaLoopCycle record, @Param("example") MarketingTcyrCpaLoopCycleExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaLoopCycle record, @Param("example") MarketingTcyrCpaLoopCycleExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaLoopCycle record);

    int updateByPrimaryKey(MarketingTcyrCpaLoopCycle record);
}