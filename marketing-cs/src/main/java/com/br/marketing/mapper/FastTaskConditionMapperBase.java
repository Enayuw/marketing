package com.br.marketing.mapper;

import com.br.marketing.entity.FastTaskCondition;
import com.br.marketing.entity.FastTaskConditionExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FastTaskConditionMapperBase {
    int countByExample(FastTaskConditionExample example);

    int deleteByExample(FastTaskConditionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FastTaskCondition record);

    int insertSelective(FastTaskCondition record);

    List<FastTaskCondition> selectByExample(FastTaskConditionExample example);

    FastTaskCondition selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FastTaskCondition record, @Param("example") FastTaskConditionExample example);

    int updateByExample(@Param("record") FastTaskCondition record, @Param("example") FastTaskConditionExample example);

    int updateByPrimaryKeySelective(FastTaskCondition record);

    int updateByPrimaryKey(FastTaskCondition record);
}