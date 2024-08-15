package com.br.marketing.mapper.score;

import com.br.marketing.entity.score.ScoreCustomerStrategyProductField;
import com.br.marketing.entity.score.ScoreCustomerStrategyProductFieldExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScoreCustomerStrategyProductFieldMapperBase {
    int countByExample(ScoreCustomerStrategyProductFieldExample example);

    int deleteByExample(ScoreCustomerStrategyProductFieldExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ScoreCustomerStrategyProductField record);

    int insertSelective(ScoreCustomerStrategyProductField record);

    List<ScoreCustomerStrategyProductField> selectByExample(ScoreCustomerStrategyProductFieldExample example);

    ScoreCustomerStrategyProductField selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ScoreCustomerStrategyProductField record, @Param("example") ScoreCustomerStrategyProductFieldExample example);

    int updateByExample(@Param("record") ScoreCustomerStrategyProductField record, @Param("example") ScoreCustomerStrategyProductFieldExample example);

    int updateByPrimaryKeySelective(ScoreCustomerStrategyProductField record);

    int updateByPrimaryKey(ScoreCustomerStrategyProductField record);
}