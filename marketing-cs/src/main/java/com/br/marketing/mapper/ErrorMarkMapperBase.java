package com.br.marketing.mapper;

import com.br.marketing.entity.ErrorMark;
import com.br.marketing.entity.ErrorMarkExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ErrorMarkMapperBase {
    int countByExample(ErrorMarkExample example);

    int deleteByExample(ErrorMarkExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ErrorMark record);

    int insertSelective(ErrorMark record);

    List<ErrorMark> selectByExample(ErrorMarkExample example);

    ErrorMark selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ErrorMark record, @Param("example") ErrorMarkExample example);

    int updateByExample(@Param("record") ErrorMark record, @Param("example") ErrorMarkExample example);

    int updateByPrimaryKeySelective(ErrorMark record);

    int updateByPrimaryKey(ErrorMark record);
}