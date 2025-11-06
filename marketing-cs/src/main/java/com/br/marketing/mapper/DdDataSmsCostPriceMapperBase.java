package com.br.marketing.mapper;

import com.br.marketing.entity.DdDataSmsCostPrice;
import com.br.marketing.entity.DdDataSmsCostPriceExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DdDataSmsCostPriceMapperBase {
    int countByExample(DdDataSmsCostPriceExample example);

    int deleteByExample(DdDataSmsCostPriceExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DdDataSmsCostPrice record);

    int insertSelective(DdDataSmsCostPrice record);

    List<DdDataSmsCostPrice> selectByExample(DdDataSmsCostPriceExample example);

    DdDataSmsCostPrice selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DdDataSmsCostPrice record, @Param("example") DdDataSmsCostPriceExample example);

    int updateByExample(@Param("record") DdDataSmsCostPrice record, @Param("example") DdDataSmsCostPriceExample example);

    int updateByPrimaryKeySelective(DdDataSmsCostPrice record);

    int updateByPrimaryKey(DdDataSmsCostPrice record);
}