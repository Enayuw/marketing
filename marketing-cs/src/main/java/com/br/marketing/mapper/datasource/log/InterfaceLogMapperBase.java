package com.br.marketing.mapper.datasource.log;

import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.entity.InterfaceLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InterfaceLogMapperBase {
    int countByExample(InterfaceLogExample example);

    int deleteByExample(InterfaceLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(InterfaceLog record);

    int insertSelective(InterfaceLog record);

    List<InterfaceLog> selectByExample(InterfaceLogExample example);

    InterfaceLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") InterfaceLog record, @Param("example") InterfaceLogExample example);

    int updateByExample(@Param("record") InterfaceLog record, @Param("example") InterfaceLogExample example);

    int updateByPrimaryKeySelective(InterfaceLog record);

    int updateByPrimaryKey(InterfaceLog record);
}