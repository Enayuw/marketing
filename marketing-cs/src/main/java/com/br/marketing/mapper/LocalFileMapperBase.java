package com.br.marketing.mapper;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LocalFileMapperBase {
    int countByExample(LocalFileExample example);

    int deleteByExample(LocalFileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(LocalFile record);

    int insertSelective(LocalFile record);

    List<LocalFile> selectByExample(LocalFileExample example);

    LocalFile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") LocalFile record, @Param("example") LocalFileExample example);

    int updateByExample(@Param("record") LocalFile record, @Param("example") LocalFileExample example);

    int updateByPrimaryKeySelective(LocalFile record);

    int updateByPrimaryKey(LocalFile record);
}