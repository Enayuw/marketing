package com.br.marketing.mapper;

import com.br.marketing.entity.CustomerRule;
import com.br.marketing.entity.CustomerRuleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerRuleMapperBase {
    int countByExample(CustomerRuleExample example);

    int deleteByExample(CustomerRuleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CustomerRule record);

    int insertSelective(CustomerRule record);

    List<CustomerRule> selectByExample(CustomerRuleExample example);

    CustomerRule selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CustomerRule record, @Param("example") CustomerRuleExample example);

    int updateByExample(@Param("record") CustomerRule record, @Param("example") CustomerRuleExample example);

    int updateByPrimaryKeySelective(CustomerRule record);

    int updateByPrimaryKey(CustomerRule record);
}