package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSmsAccountLog;
import com.br.marketing.entity.MarketingSmsAccountLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSmsAccountLogMapperBase {
    int countByExample(MarketingSmsAccountLogExample example);

    int deleteByExample(MarketingSmsAccountLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingSmsAccountLog record);

    int insertSelective(MarketingSmsAccountLog record);

    List<MarketingSmsAccountLog> selectByExample(MarketingSmsAccountLogExample example);

    MarketingSmsAccountLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingSmsAccountLog record, @Param("example") MarketingSmsAccountLogExample example);

    int updateByExample(@Param("record") MarketingSmsAccountLog record, @Param("example") MarketingSmsAccountLogExample example);

    int updateByPrimaryKeySelective(MarketingSmsAccountLog record);

    int updateByPrimaryKey(MarketingSmsAccountLog record);
}