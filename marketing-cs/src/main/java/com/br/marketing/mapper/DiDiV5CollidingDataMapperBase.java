package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiV5CollidingData;
import com.br.marketing.entity.DiDiV5CollidingDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DiDiV5CollidingDataMapperBase {
    int countByExample(DiDiV5CollidingDataExample example);

    int deleteByExample(DiDiV5CollidingDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DiDiV5CollidingData record);

    int insertSelective(DiDiV5CollidingData record);

    List<DiDiV5CollidingData> selectByExample(DiDiV5CollidingDataExample example);

    DiDiV5CollidingData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DiDiV5CollidingData record, @Param("example") DiDiV5CollidingDataExample example);

    int updateByExample(@Param("record") DiDiV5CollidingData record, @Param("example") DiDiV5CollidingDataExample example);

    int updateByPrimaryKeySelective(DiDiV5CollidingData record);

    int updateByPrimaryKey(DiDiV5CollidingData record);
}