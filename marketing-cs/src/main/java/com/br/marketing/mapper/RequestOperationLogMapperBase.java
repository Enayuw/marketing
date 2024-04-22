package com.br.marketing.mapper;

import com.br.marketing.entity.RequestOperationLog;
import com.br.marketing.entity.RequestOperationLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RequestOperationLogMapperBase {
    int countByExample(RequestOperationLogExample example);

    int deleteByExample(RequestOperationLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RequestOperationLog record);

    int insertSelective(RequestOperationLog record);

    List<RequestOperationLog> selectByExample(RequestOperationLogExample example);

    RequestOperationLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") RequestOperationLog record, @Param("example") RequestOperationLogExample example);

    int updateByExample(@Param("record") RequestOperationLog record, @Param("example") RequestOperationLogExample example);

    int updateByPrimaryKeySelective(RequestOperationLog record);

    int updateByPrimaryKey(RequestOperationLog record);
}