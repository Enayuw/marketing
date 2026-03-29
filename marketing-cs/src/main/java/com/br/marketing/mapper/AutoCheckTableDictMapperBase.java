package com.br.marketing.mapper;

import com.br.marketing.entity.AutoCheckTableDict;
import com.br.marketing.entity.AutoCheckTableDictExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AutoCheckTableDictMapperBase {
    int countByExample(AutoCheckTableDictExample example);

    int deleteByExample(AutoCheckTableDictExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AutoCheckTableDict record);

    int insertSelective(AutoCheckTableDict record);

    List<AutoCheckTableDict> selectByExample(AutoCheckTableDictExample example);

    AutoCheckTableDict selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AutoCheckTableDict record, @Param("example") AutoCheckTableDictExample example);

    int updateByExample(@Param("record") AutoCheckTableDict record, @Param("example") AutoCheckTableDictExample example);

    int updateByPrimaryKeySelective(AutoCheckTableDict record);

    int updateByPrimaryKey(AutoCheckTableDict record);
}