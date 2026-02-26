package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiCollidingDataRob;
import com.br.marketing.entity.DiDiCollidingDataRobExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DiDiV5CollidingDataRobMapperBase {
    int countByExample(DiDiCollidingDataRobExample example);

    int deleteByExample(DiDiCollidingDataRobExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DiDiCollidingDataRob record);

    int insertSelective(DiDiCollidingDataRob record);

    List<DiDiCollidingDataRob> selectByExample(DiDiCollidingDataRobExample example);

    DiDiCollidingDataRob selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DiDiCollidingDataRob record, @Param("example") DiDiCollidingDataRobExample example);

    int updateByExample(@Param("record") DiDiCollidingDataRob record, @Param("example") DiDiCollidingDataRobExample example);

    int updateByPrimaryKeySelective(DiDiCollidingDataRob record);

    int updateByPrimaryKey(DiDiCollidingDataRob record);
}