package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.ShuHeCuFuJieData;
import com.br.marketing.entity.ShuHeCuFuJieDataExample;

public interface ShuHeCuFuJieDataMapperBase {
    int countByExample(ShuHeCuFuJieDataExample example);

    int deleteByExample(ShuHeCuFuJieDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ShuHeCuFuJieData record);

    int insertSelective(ShuHeCuFuJieData record);

    List<ShuHeCuFuJieData> selectByExample(ShuHeCuFuJieDataExample example);

    ShuHeCuFuJieData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ShuHeCuFuJieData record, @Param("example") ShuHeCuFuJieDataExample example);

    int updateByExample(@Param("record") ShuHeCuFuJieData record, @Param("example") ShuHeCuFuJieDataExample example);

    int updateByPrimaryKeySelective(ShuHeCuFuJieData record);

    int updateByPrimaryKey(ShuHeCuFuJieData record);
}