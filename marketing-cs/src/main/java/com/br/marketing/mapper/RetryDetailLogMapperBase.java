package com.br.marketing.mapper;

import com.br.marketing.entity.RetryDetailLog;
import com.br.marketing.entity.RetryDetailLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RetryDetailLogMapperBase {
    int countByExample(RetryDetailLogExample example);

    int deleteByExample(RetryDetailLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RetryDetailLog record);

    int insertSelective(RetryDetailLog record);

    List<RetryDetailLog> selectByExample(RetryDetailLogExample example);

    RetryDetailLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") RetryDetailLog record, @Param("example") RetryDetailLogExample example);

    int updateByExample(@Param("record") RetryDetailLog record, @Param("example") RetryDetailLogExample example);

    int updateByPrimaryKeySelective(RetryDetailLog record);

    int updateByPrimaryKey(RetryDetailLog record);
}