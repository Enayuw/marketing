package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiDataLoopCycle;
import com.br.marketing.entity.DiDiDataLoopCycleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DiDiV5DataLoopCycleMapperBase {
    int countByExample(DiDiDataLoopCycleExample example);

    int deleteByExample(DiDiDataLoopCycleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DiDiDataLoopCycle record);

    int insertSelective(DiDiDataLoopCycle record);

    List<DiDiDataLoopCycle> selectByExample(DiDiDataLoopCycleExample example);

    DiDiDataLoopCycle selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DiDiDataLoopCycle record, @Param("example") DiDiDataLoopCycleExample example);

    int updateByExample(@Param("record") DiDiDataLoopCycle record, @Param("example") DiDiDataLoopCycleExample example);

    int updateByPrimaryKeySelective(DiDiDataLoopCycle record);

    int updateByPrimaryKey(DiDiDataLoopCycle record);
}