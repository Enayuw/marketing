package com.br.marketing.mapper;

import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.LoadResultExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoadResultMapperBase {
    int countByExample(LoadResultExample example);

    int deleteByExample(LoadResultExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LoadResult record);

    int insertSelective(LoadResult record);

    List<LoadResult> selectByExample(LoadResultExample example);

    LoadResult selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") LoadResult record, @Param("example") LoadResultExample example);

    int updateByExample(@Param("record") LoadResult record, @Param("example") LoadResultExample example);

    int updateByPrimaryKeySelective(LoadResult record);

    int updateByPrimaryKey(LoadResult record);
}