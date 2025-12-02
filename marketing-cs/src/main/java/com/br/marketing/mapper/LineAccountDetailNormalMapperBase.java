package com.br.marketing.mapper;

import com.br.marketing.entity.LineAccountDetailNormal;
import com.br.marketing.entity.LineAccountDetailNormalExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineAccountDetailNormalMapperBase {
    int countByExample(LineAccountDetailNormalExample example);

    int deleteByExample(LineAccountDetailNormalExample example);

    int deleteByPrimaryKey(Long id);

    int insert(LineAccountDetailNormal record);

    int insertSelective(LineAccountDetailNormal record);

    List<LineAccountDetailNormal> selectByExample(LineAccountDetailNormalExample example);

    LineAccountDetailNormal selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") LineAccountDetailNormal record, @Param("example") LineAccountDetailNormalExample example);

    int updateByExample(@Param("record") LineAccountDetailNormal record, @Param("example") LineAccountDetailNormalExample example);

    int updateByPrimaryKeySelective(LineAccountDetailNormal record);

    int updateByPrimaryKey(LineAccountDetailNormal record);
}