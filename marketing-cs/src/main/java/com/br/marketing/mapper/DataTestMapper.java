package com.br.marketing.mapper;

import com.br.marketing.entity.DataTest;
import com.br.marketing.entity.DataTestExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataTestMapper {
    int countByExample(DataTestExample example);

    int deleteByExample(DataTestExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DataTest record);

    int insertSelective(DataTest record);

    List<DataTest> selectByExample(DataTestExample example);

    DataTest selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DataTest record, @Param("example") DataTestExample example);

    int updateByExample(@Param("record") DataTest record, @Param("example") DataTestExample example);

    int updateByPrimaryKeySelective(DataTest record);

    int updateByPrimaryKey(DataTest record);
}