package com.br.marketing.mapper;

import com.br.marketing.entity.DdDataLineCostPrice;
import com.br.marketing.entity.DdDataLineCostPriceExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DdDataLineCostPriceMapperBase {
    int countByExample(DdDataLineCostPriceExample example);

    int deleteByExample(DdDataLineCostPriceExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DdDataLineCostPrice record);

    int insertSelective(DdDataLineCostPrice record);

    List<DdDataLineCostPrice> selectByExample(DdDataLineCostPriceExample example);

    DdDataLineCostPrice selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DdDataLineCostPrice record, @Param("example") DdDataLineCostPriceExample example);

    int updateByExample(@Param("record") DdDataLineCostPrice record, @Param("example") DdDataLineCostPriceExample example);

    int updateByPrimaryKeySelective(DdDataLineCostPrice record);

    int updateByPrimaryKey(DdDataLineCostPrice record);
}