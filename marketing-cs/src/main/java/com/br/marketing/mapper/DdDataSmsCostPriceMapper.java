package com.br.marketing.mapper;

import com.br.marketing.entity.DdDataSmsCostPrice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DdDataSmsCostPriceMapper extends DdDataSmsCostPriceMapperBase{

    List<DdDataSmsCostPrice> selectList(@Param("searchId") Long searchId,
                                        @Param("searchSize") Integer searchSize);
}