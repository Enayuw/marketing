package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiV5CollidingDataLog;
import com.br.marketing.entity.DiDiV5CollidingDataLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DiDiV5CollidingDataLogMapperBase {
    int countByExample(DiDiV5CollidingDataLogExample example);

    int deleteByExample(DiDiV5CollidingDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DiDiV5CollidingDataLog record);

    int insertSelective(DiDiV5CollidingDataLog record);

    List<DiDiV5CollidingDataLog> selectByExample(DiDiV5CollidingDataLogExample example);

    DiDiV5CollidingDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DiDiV5CollidingDataLog record, @Param("example") DiDiV5CollidingDataLogExample example);

    int updateByExample(@Param("record") DiDiV5CollidingDataLog record, @Param("example") DiDiV5CollidingDataLogExample example);

    int updateByPrimaryKeySelective(DiDiV5CollidingDataLog record);

    int updateByPrimaryKey(DiDiV5CollidingDataLog record);
}