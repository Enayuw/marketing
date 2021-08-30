package com.br.marketing.mapper;

import com.br.marketing.entity.VariableDic;
import com.br.marketing.entity.VariableDicExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VariableDicMapperBase {
    int countByExample(VariableDicExample example);

    int deleteByExample(VariableDicExample example);

    int deleteByPrimaryKey(Long id);

    int insert(VariableDic record);

    int insertSelective(VariableDic record);

    List<VariableDic> selectByExample(VariableDicExample example);

    VariableDic selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") VariableDic record, @Param("example") VariableDicExample example);

    int updateByExample(@Param("record") VariableDic record, @Param("example") VariableDicExample example);

    int updateByPrimaryKeySelective(VariableDic record);

    int updateByPrimaryKey(VariableDic record);
}