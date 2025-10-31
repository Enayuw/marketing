package com.br.marketing.mapper;

import com.br.marketing.entity.DdDataLineCostPrice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DdDataLineCostPriceMapper extends DdDataLineCostPriceMapperBase{

    List<DdDataLineCostPrice> selectList(@Param("searchId") Long searchId,
                                         @Param("searchSize") Integer searchSize);
}