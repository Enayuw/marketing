package com.br.marketing.mapper;

import com.br.marketing.entity.RetryMainLog;
import com.br.marketing.entity.RetryMainLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RetryMainLogMapperBase {
    int countByExample(RetryMainLogExample example);

    int deleteByExample(RetryMainLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RetryMainLog record);

    int insertSelective(RetryMainLog record);

    List<RetryMainLog> selectByExample(RetryMainLogExample example);

    RetryMainLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") RetryMainLog record, @Param("example") RetryMainLogExample example);

    int updateByExample(@Param("record") RetryMainLog record, @Param("example") RetryMainLogExample example);

    int updateByPrimaryKeySelective(RetryMainLog record);

    int updateByPrimaryKey(RetryMainLog record);
}