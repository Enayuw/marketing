package com.br.marketing.mapper;

import com.br.marketing.entity.ScoreOptLog;
import com.br.marketing.entity.ScoreOptLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScoreOptLogMapperBase {
    int countByExample(ScoreOptLogExample example);

    int deleteByExample(ScoreOptLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ScoreOptLog record);

    int insertSelective(ScoreOptLog record);

    List<ScoreOptLog> selectByExample(ScoreOptLogExample example);

    ScoreOptLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ScoreOptLog record, @Param("example") ScoreOptLogExample example);

    int updateByExample(@Param("record") ScoreOptLog record, @Param("example") ScoreOptLogExample example);

    int updateByPrimaryKeySelective(ScoreOptLog record);

    int updateByPrimaryKey(ScoreOptLog record);
}