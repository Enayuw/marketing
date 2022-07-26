package com.br.marketing.mapper;

import com.br.marketing.entity.ScoreSearchConditionMapping;
import com.br.marketing.entity.ScoreSearchConditionMappingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScoreSearchConditionMappingMapperBase {
    int countByExample(ScoreSearchConditionMappingExample example);

    int deleteByExample(ScoreSearchConditionMappingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ScoreSearchConditionMapping record);

    int insertSelective(ScoreSearchConditionMapping record);

    List<ScoreSearchConditionMapping> selectByExample(ScoreSearchConditionMappingExample example);

    ScoreSearchConditionMapping selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ScoreSearchConditionMapping record, @Param("example") ScoreSearchConditionMappingExample example);

    int updateByExample(@Param("record") ScoreSearchConditionMapping record, @Param("example") ScoreSearchConditionMappingExample example);

    int updateByPrimaryKeySelective(ScoreSearchConditionMapping record);

    int updateByPrimaryKey(ScoreSearchConditionMapping record);
}