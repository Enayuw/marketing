package com.br.marketing.mapper;

import com.br.marketing.entity.GroupStrategyConfig;
import com.br.marketing.entity.GroupStrategyConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GroupStrategyConfigMapperBase {
    int countByExample(GroupStrategyConfigExample example);

    int deleteByExample(GroupStrategyConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(GroupStrategyConfig record);

    int insertSelective(GroupStrategyConfig record);

    List<GroupStrategyConfig> selectByExample(GroupStrategyConfigExample example);

    GroupStrategyConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") GroupStrategyConfig record, @Param("example") GroupStrategyConfigExample example);

    int updateByExample(@Param("record") GroupStrategyConfig record, @Param("example") GroupStrategyConfigExample example);

    int updateByPrimaryKeySelective(GroupStrategyConfig record);

    int updateByPrimaryKey(GroupStrategyConfig record);
}