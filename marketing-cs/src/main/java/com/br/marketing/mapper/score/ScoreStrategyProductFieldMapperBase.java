package com.br.marketing.mapper.score;

import com.br.marketing.entity.score.ScoreStrategyProductField;
import com.br.marketing.entity.score.ScoreStrategyProductFieldExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScoreStrategyProductFieldMapperBase {
    int countByExample(ScoreStrategyProductFieldExample example);

    int deleteByExample(ScoreStrategyProductFieldExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ScoreStrategyProductField record);

    int insertSelective(ScoreStrategyProductField record);

    List<ScoreStrategyProductField> selectByExample(ScoreStrategyProductFieldExample example);

    ScoreStrategyProductField selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ScoreStrategyProductField record, @Param("example") ScoreStrategyProductFieldExample example);

    int updateByExample(@Param("record") ScoreStrategyProductField record, @Param("example") ScoreStrategyProductFieldExample example);

    int updateByPrimaryKeySelective(ScoreStrategyProductField record);

    int updateByPrimaryKey(ScoreStrategyProductField record);
}