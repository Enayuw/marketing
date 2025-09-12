package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSanLiuLingCollection;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketingSanLiuLingCollectionMapper extends MarketingSanLiuLingCollectionMapperBase{

    int batchInsert(@Param("list") List<MarketingSanLiuLingCollection> list);

}
