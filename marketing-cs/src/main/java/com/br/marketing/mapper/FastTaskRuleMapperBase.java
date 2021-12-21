package com.br.marketing.mapper;

import com.br.marketing.entity.FastTaskRule;
import com.br.marketing.entity.FastTaskRuleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FastTaskRuleMapperBase {
    int countByExample(FastTaskRuleExample example);

    int deleteByExample(FastTaskRuleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FastTaskRule record);

    int insertSelective(FastTaskRule record);

    List<FastTaskRule> selectByExample(FastTaskRuleExample example);

    FastTaskRule selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FastTaskRule record, @Param("example") FastTaskRuleExample example);

    int updateByExample(@Param("record") FastTaskRule record, @Param("example") FastTaskRuleExample example);

    int updateByPrimaryKeySelective(FastTaskRule record);

    int updateByPrimaryKey(FastTaskRule record);
}