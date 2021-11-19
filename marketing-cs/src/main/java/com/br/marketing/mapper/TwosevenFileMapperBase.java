package com.br.marketing.mapper;

import com.br.marketing.entity.TwosevenFile;
import com.br.marketing.entity.TwosevenFileExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TwosevenFileMapperBase {
    int countByExample(TwosevenFileExample example);

    int deleteByExample(TwosevenFileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TwosevenFile record);

    int insertSelective(TwosevenFile record);

    List<TwosevenFile> selectByExample(TwosevenFileExample example);

    TwosevenFile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TwosevenFile record, @Param("example") TwosevenFileExample example);

    int updateByExample(@Param("record") TwosevenFile record, @Param("example") TwosevenFileExample example);

    int updateByPrimaryKeySelective(TwosevenFile record);

    int updateByPrimaryKey(TwosevenFile record);
}