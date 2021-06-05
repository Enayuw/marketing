package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingStrategyProduct;
import com.br.marketing.entity.MarketingStrategyProductExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingStrategyProductMapperBase {
    int countByExample(MarketingStrategyProductExample example);

    int deleteByExample(MarketingStrategyProductExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingStrategyProduct record);

    int insertSelective(MarketingStrategyProduct record);

    List<MarketingStrategyProduct> selectByExample(MarketingStrategyProductExample example);

    MarketingStrategyProduct selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingStrategyProduct record, @Param("example") MarketingStrategyProductExample example);

    int updateByExample(@Param("record") MarketingStrategyProduct record, @Param("example") MarketingStrategyProductExample example);

    int updateByPrimaryKeySelective(MarketingStrategyProduct record);

    int updateByPrimaryKey(MarketingStrategyProduct record);
}