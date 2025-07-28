package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDict;
import com.br.marketing.entity.MarketingDictExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDictMapperBase {
    int countByExample(MarketingDictExample example);

    int deleteByExample(MarketingDictExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingDict record);

    int insertSelective(MarketingDict record);

    List<MarketingDict> selectByExample(MarketingDictExample example);

    MarketingDict selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingDict record, @Param("example") MarketingDictExample example);

    int updateByExample(@Param("record") MarketingDict record, @Param("example") MarketingDictExample example);

    int updateByPrimaryKeySelective(MarketingDict record);

    int updateByPrimaryKey(MarketingDict record);
}