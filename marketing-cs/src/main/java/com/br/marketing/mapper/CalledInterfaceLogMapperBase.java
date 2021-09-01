package com.br.marketing.mapper;

import com.br.marketing.entity.CalledInterfaceLog;
import com.br.marketing.entity.CalledInterfaceLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CalledInterfaceLogMapperBase {
    int countByExample(CalledInterfaceLogExample example);

    int deleteByExample(CalledInterfaceLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CalledInterfaceLog record);

    int insertSelective(CalledInterfaceLog record);

    List<CalledInterfaceLog> selectByExample(CalledInterfaceLogExample example);

    CalledInterfaceLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CalledInterfaceLog record, @Param("example") CalledInterfaceLogExample example);

    int updateByExample(@Param("record") CalledInterfaceLog record, @Param("example") CalledInterfaceLogExample example);

    int updateByPrimaryKeySelective(CalledInterfaceLog record);

    int updateByPrimaryKey(CalledInterfaceLog record);
}