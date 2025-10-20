package com.br.marketing.mapper;

import com.br.marketing.entity.MockPolicy;
import com.br.marketing.entity.MockPolicyExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MockPolicyMapperBase {
    int countByExample(MockPolicyExample example);

    int deleteByExample(MockPolicyExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MockPolicy record);

    int insertSelective(MockPolicy record);

    List<MockPolicy> selectByExample(MockPolicyExample example);

    MockPolicy selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MockPolicy record, @Param("example") MockPolicyExample example);

    int updateByExample(@Param("record") MockPolicy record, @Param("example") MockPolicyExample example);

    int updateByPrimaryKeySelective(MockPolicy record);

    int updateByPrimaryKey(MockPolicy record);
}