package com.br.marketing.mapper;

import com.br.marketing.entity.SoleRuleConfig;
import com.br.marketing.entity.SoleRuleConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SoleRuleConfigMapperBase {
    int countByExample(SoleRuleConfigExample example);

    int deleteByExample(SoleRuleConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SoleRuleConfig record);

    int insertSelective(SoleRuleConfig record);

    List<SoleRuleConfig> selectByExample(SoleRuleConfigExample example);

    SoleRuleConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SoleRuleConfig record, @Param("example") SoleRuleConfigExample example);

    int updateByExample(@Param("record") SoleRuleConfig record, @Param("example") SoleRuleConfigExample example);

    int updateByPrimaryKeySelective(SoleRuleConfig record);

    int updateByPrimaryKey(SoleRuleConfig record);
}