package com.br.marketing.mapper;

import com.br.marketing.entity.RequestInterfaceLog;
import com.br.marketing.entity.RequestInterfaceLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RequestInterfaceLogMapperBase {
    int countByExample(RequestInterfaceLogExample example);

    int deleteByExample(RequestInterfaceLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RequestInterfaceLog record);

    int insertSelective(RequestInterfaceLog record);

    List<RequestInterfaceLog> selectByExample(RequestInterfaceLogExample example);

    RequestInterfaceLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") RequestInterfaceLog record, @Param("example") RequestInterfaceLogExample example);

    int updateByExample(@Param("record") RequestInterfaceLog record, @Param("example") RequestInterfaceLogExample example);

    int updateByPrimaryKeySelective(RequestInterfaceLog record);

    int updateByPrimaryKey(RequestInterfaceLog record);
}