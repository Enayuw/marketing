package com.br.marketing.mapper;

import com.br.marketing.entity.DidiCallbackDataLog;
import com.br.marketing.entity.DidiCallbackDataLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DidiCallbackDataLogMapperBase {
    int countByExample(DidiCallbackDataLogExample example);

    int deleteByExample(DidiCallbackDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DidiCallbackDataLog record);

    int insertSelective(DidiCallbackDataLog record);

    List<DidiCallbackDataLog> selectByExample(DidiCallbackDataLogExample example);

    DidiCallbackDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DidiCallbackDataLog record, @Param("example") DidiCallbackDataLogExample example);

    int updateByExample(@Param("record") DidiCallbackDataLog record, @Param("example") DidiCallbackDataLogExample example);

    int updateByPrimaryKeySelective(DidiCallbackDataLog record);

    int updateByPrimaryKey(DidiCallbackDataLog record);
}