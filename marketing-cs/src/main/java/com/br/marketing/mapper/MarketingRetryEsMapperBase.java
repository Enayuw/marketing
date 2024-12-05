package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingRetryEs;
import com.br.marketing.entity.MarketingRetryEsExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingRetryEsMapperBase {
    int countByExample(MarketingRetryEsExample example);

    int deleteByExample(MarketingRetryEsExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingRetryEs record);

    int insertSelective(MarketingRetryEs record);

    List<MarketingRetryEs> selectByExample(MarketingRetryEsExample example);

    MarketingRetryEs selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingRetryEs record, @Param("example") MarketingRetryEsExample example);

    int updateByExample(@Param("record") MarketingRetryEs record, @Param("example") MarketingRetryEsExample example);

    int updateByPrimaryKeySelective(MarketingRetryEs record);

    int updateByPrimaryKey(MarketingRetryEs record);
}