package com.br.marketing.mapper;

import com.br.marketing.entity.LineBaseInfoNormal;
import com.br.marketing.entity.LineBaseInfoNormalExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineBaseInfoNormalMapperBase {
    int countByExample(LineBaseInfoNormalExample example);

    int deleteByExample(LineBaseInfoNormalExample example);

    int deleteByPrimaryKey(Long id);

    int insert(LineBaseInfoNormal record);

    int insertSelective(LineBaseInfoNormal record);

    List<LineBaseInfoNormal> selectByExample(LineBaseInfoNormalExample example);

    LineBaseInfoNormal selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") LineBaseInfoNormal record, @Param("example") LineBaseInfoNormalExample example);

    int updateByExample(@Param("record") LineBaseInfoNormal record, @Param("example") LineBaseInfoNormalExample example);

    int updateByPrimaryKeySelective(LineBaseInfoNormal record);

    int updateByPrimaryKey(LineBaseInfoNormal record);
}