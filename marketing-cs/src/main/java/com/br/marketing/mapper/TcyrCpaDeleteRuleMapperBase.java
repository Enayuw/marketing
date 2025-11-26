package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaDeleteRule;
import com.br.marketing.entity.TcyrCpaDeleteRuleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaDeleteRuleMapperBase {
    int countByExample(TcyrCpaDeleteRuleExample example);

    int deleteByExample(TcyrCpaDeleteRuleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaDeleteRule record);

    int insertSelective(TcyrCpaDeleteRule record);

    List<TcyrCpaDeleteRule> selectByExample(TcyrCpaDeleteRuleExample example);

    TcyrCpaDeleteRule selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaDeleteRule record, @Param("example") TcyrCpaDeleteRuleExample example);

    int updateByExample(@Param("record") TcyrCpaDeleteRule record, @Param("example") TcyrCpaDeleteRuleExample example);

    int updateByPrimaryKeySelective(TcyrCpaDeleteRule record);

    int updateByPrimaryKey(TcyrCpaDeleteRule record);
}