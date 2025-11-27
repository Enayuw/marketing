package com.br.marketing.mapper;

import com.br.marketing.entity.LineAccountLogNormal;
import com.br.marketing.entity.LineAccountLogNormalExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineAccountLogNormalMapperBase {
    int countByExample(LineAccountLogNormalExample example);

    int deleteByExample(LineAccountLogNormalExample example);

    int deleteByPrimaryKey(Long id);

    int insert(LineAccountLogNormal record);

    int insertSelective(LineAccountLogNormal record);

    List<LineAccountLogNormal> selectByExample(LineAccountLogNormalExample example);

    LineAccountLogNormal selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") LineAccountLogNormal record, @Param("example") LineAccountLogNormalExample example);

    int updateByExample(@Param("record") LineAccountLogNormal record, @Param("example") LineAccountLogNormalExample example);

    int updateByPrimaryKeySelective(LineAccountLogNormal record);

    int updateByPrimaryKey(LineAccountLogNormal record);
}