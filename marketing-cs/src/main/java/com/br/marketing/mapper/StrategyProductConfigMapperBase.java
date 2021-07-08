package com.br.marketing.mapper;

import com.br.marketing.entity.StrategyProductConfig;
import com.br.marketing.entity.StrategyProductConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StrategyProductConfigMapperBase {
    int countByExample(StrategyProductConfigExample example);

    int deleteByExample(StrategyProductConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(StrategyProductConfig record);

    int insertSelective(StrategyProductConfig record);

    List<StrategyProductConfig> selectByExample(StrategyProductConfigExample example);

    StrategyProductConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") StrategyProductConfig record, @Param("example") StrategyProductConfigExample example);

    int updateByExample(@Param("record") StrategyProductConfig record, @Param("example") StrategyProductConfigExample example);

    int updateByPrimaryKeySelective(StrategyProductConfig record);

    int updateByPrimaryKey(StrategyProductConfig record);
}