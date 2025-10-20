package com.br.marketing.mapper;

import com.br.marketing.entity.MockCase;
import com.br.marketing.entity.MockCaseExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MockCaseMapperBase {
    int countByExample(MockCaseExample example);

    int deleteByExample(MockCaseExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MockCase record);

    int insertSelective(MockCase record);

    List<MockCase> selectByExample(MockCaseExample example);

    MockCase selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MockCase record, @Param("example") MockCaseExample example);

    int updateByExample(@Param("record") MockCase record, @Param("example") MockCaseExample example);

    int updateByPrimaryKeySelective(MockCase record);

    int updateByPrimaryKey(MockCase record);
}