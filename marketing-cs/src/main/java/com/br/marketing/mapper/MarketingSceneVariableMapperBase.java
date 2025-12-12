package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSceneVariable;
import com.br.marketing.entity.MarketingSceneVariableExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSceneVariableMapperBase {
    int countByExample(MarketingSceneVariableExample example);

    int deleteByExample(MarketingSceneVariableExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingSceneVariable record);

    int insertSelective(MarketingSceneVariable record);

    List<MarketingSceneVariable> selectByExample(MarketingSceneVariableExample example);

    MarketingSceneVariable selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingSceneVariable record, @Param("example") MarketingSceneVariableExample example);

    int updateByExample(@Param("record") MarketingSceneVariable record, @Param("example") MarketingSceneVariableExample example);

    int updateByPrimaryKeySelective(MarketingSceneVariable record);

    int updateByPrimaryKey(MarketingSceneVariable record);
}