package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSep;
import com.br.marketing.entity.MarketingSepExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSepMapperBase {
    int countByExample(MarketingSepExample example);

    int deleteByExample(MarketingSepExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(MarketingSep record);

    int insertSelective(MarketingSep record);

    List<MarketingSep> selectByExample(MarketingSepExample example);

    MarketingSep selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") MarketingSep record, @Param("example") MarketingSepExample example);

    int updateByExample(@Param("record") MarketingSep record, @Param("example") MarketingSepExample example);

    int updateByPrimaryKeySelective(MarketingSep record);

    int updateByPrimaryKey(MarketingSep record);
}