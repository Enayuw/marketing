package com.br.marketing.mapper;

import com.br.marketing.entity.AutoCheckSenceDict;
import com.br.marketing.entity.AutoCheckSenceDictExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AutoCheckSenceDictMapperBase {
    int countByExample(AutoCheckSenceDictExample example);

    int deleteByExample(AutoCheckSenceDictExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AutoCheckSenceDict record);

    int insertSelective(AutoCheckSenceDict record);

    List<AutoCheckSenceDict> selectByExample(AutoCheckSenceDictExample example);

    AutoCheckSenceDict selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AutoCheckSenceDict record, @Param("example") AutoCheckSenceDictExample example);

    int updateByExample(@Param("record") AutoCheckSenceDict record, @Param("example") AutoCheckSenceDictExample example);

    int updateByPrimaryKeySelective(AutoCheckSenceDict record);

    int updateByPrimaryKey(AutoCheckSenceDict record);
}