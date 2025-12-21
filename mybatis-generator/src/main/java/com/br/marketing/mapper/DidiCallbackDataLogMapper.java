package com.br.marketing.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DidiCallbackDataLogMapper {
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