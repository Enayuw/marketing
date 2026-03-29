package com.br.marketing.mapper;

import com.br.marketing.entity.DidiV5BlackDataLog;
import com.br.marketing.entity.DidiV5BlackDataLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DidiV5BlackDataLogMapper {
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