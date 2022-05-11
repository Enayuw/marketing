package com.br.marketing.mapper;

import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.entity.ScoreRuleConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ScoreRuleConfigMapper {
    int countByExample(ScoreRuleConfigExample example);

    int deleteByExample(ScoreRuleConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ScoreRuleConfig record);

    int insertSelective(ScoreRuleConfig record);

    List<ScoreRuleConfig> selectByExample(ScoreRuleConfigExample example);

    ScoreRuleConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ScoreRuleConfig record, @Param("example") ScoreRuleConfigExample example);

    int updateByExample(@Param("record") ScoreRuleConfig record, @Param("example") ScoreRuleConfigExample example);

    int updateByPrimaryKeySelective(ScoreRuleConfig record);

    int updateByPrimaryKey(ScoreRuleConfig record);
}