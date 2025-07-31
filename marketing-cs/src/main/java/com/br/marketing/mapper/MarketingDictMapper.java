package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDict;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDictMapper extends MarketingDictMapperBase{

    List<MarketingDict> getDictInfo(@Param("dictType") String dictType);
}