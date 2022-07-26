package com.br.marketing.mapper;

import com.br.marketing.entity.ScoreSearchCondition;
import com.br.marketing.entity.ScoreSearchConditionExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScoreSearchConditionMapperBase {
    int countByExample(ScoreSearchConditionExample example);

    int deleteByExample(ScoreSearchConditionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ScoreSearchCondition record);

    int insertSelective(ScoreSearchCondition record);

    List<ScoreSearchCondition> selectByExample(ScoreSearchConditionExample example);

    ScoreSearchCondition selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ScoreSearchCondition record, @Param("example") ScoreSearchConditionExample example);

    int updateByExample(@Param("record") ScoreSearchCondition record, @Param("example") ScoreSearchConditionExample example);

    int updateByPrimaryKeySelective(ScoreSearchCondition record);

    int updateByPrimaryKey(ScoreSearchCondition record);
}