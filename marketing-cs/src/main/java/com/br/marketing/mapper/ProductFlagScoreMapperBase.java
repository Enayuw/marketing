package com.br.marketing.mapper;

import com.br.marketing.entity.ProductFlagScore;
import com.br.marketing.entity.ProductFlagScoreExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductFlagScoreMapperBase {
    int countByExample(ProductFlagScoreExample example);

    int deleteByExample(ProductFlagScoreExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ProductFlagScore record);

    int insertSelective(ProductFlagScore record);

    List<ProductFlagScore> selectByExample(ProductFlagScoreExample example);

    ProductFlagScore selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ProductFlagScore record, @Param("example") ProductFlagScoreExample example);

    int updateByExample(@Param("record") ProductFlagScore record, @Param("example") ProductFlagScoreExample example);

    int updateByPrimaryKeySelective(ProductFlagScore record);

    int updateByPrimaryKey(ProductFlagScore record);
}