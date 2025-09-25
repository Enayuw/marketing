package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSanLiuLingCollection;
import com.br.marketing.entity.MarketingSanLiuLingCollectionExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSanLiuLingCollectionMapperBase {
    int countByExample(MarketingSanLiuLingCollectionExample example);

    int deleteByExample(MarketingSanLiuLingCollectionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingSanLiuLingCollection record);

    int insertSelective(MarketingSanLiuLingCollection record);

    List<MarketingSanLiuLingCollection> selectByExample(MarketingSanLiuLingCollectionExample example);

    MarketingSanLiuLingCollection selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingSanLiuLingCollection record, @Param("example") MarketingSanLiuLingCollectionExample example);

    int updateByExample(@Param("record") MarketingSanLiuLingCollection record, @Param("example") MarketingSanLiuLingCollectionExample example);

    int updateByPrimaryKeySelective(MarketingSanLiuLingCollection record);

    int updateByPrimaryKey(MarketingSanLiuLingCollection record);
}