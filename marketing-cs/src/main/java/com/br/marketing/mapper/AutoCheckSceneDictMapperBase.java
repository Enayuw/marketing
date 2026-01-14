package com.br.marketing.mapper;

import com.br.marketing.entity.AutoCheckSceneDict;
import com.br.marketing.entity.AutoCheckSceneDictExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AutoCheckSceneDictMapperBase {
    int countByExample(AutoCheckSceneDictExample example);

    int deleteByExample(AutoCheckSceneDictExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AutoCheckSceneDict record);

    int insertSelective(AutoCheckSceneDict record);

    List<AutoCheckSceneDict> selectByExample(AutoCheckSceneDictExample example);

    AutoCheckSceneDict selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AutoCheckSceneDict record, @Param("example") AutoCheckSceneDictExample example);

    int updateByExample(@Param("record") AutoCheckSceneDict record, @Param("example") AutoCheckSceneDictExample example);

    int updateByPrimaryKeySelective(AutoCheckSceneDict record);

    int updateByPrimaryKey(AutoCheckSceneDict record);
}