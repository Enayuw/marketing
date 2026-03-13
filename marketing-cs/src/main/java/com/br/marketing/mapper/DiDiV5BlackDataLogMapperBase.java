package com.br.marketing.mapper;

import com.br.marketing.entity.DidiV5BlackDataLog;
import com.br.marketing.entity.DidiV5BlackDataLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DiDiV5BlackDataLogMapperBase {
    int countByExample(DidiV5BlackDataLogExample example);

    int deleteByExample(DidiV5BlackDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DidiV5BlackDataLog record);

    int insertSelective(DidiV5BlackDataLog record);

    List<DidiV5BlackDataLog> selectByExample(DidiV5BlackDataLogExample example);

    DidiV5BlackDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DidiV5BlackDataLog record, @Param("example") DidiV5BlackDataLogExample example);

    int updateByExample(@Param("record") DidiV5BlackDataLog record, @Param("example") DidiV5BlackDataLogExample example);

    int updateByPrimaryKeySelective(DidiV5BlackDataLog record);

    int updateByPrimaryKey(DidiV5BlackDataLog record);
}