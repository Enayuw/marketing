package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingMockTest;
import com.br.marketing.entity.MarketingMockTestExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingMockTestMapperBase {
    int countByExample(MarketingMockTestExample example);

    int deleteByExample(MarketingMockTestExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingMockTest record);

    int insertSelective(MarketingMockTest record);

    List<MarketingMockTest> selectByExample(MarketingMockTestExample example);

    MarketingMockTest selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingMockTest record, @Param("example") MarketingMockTestExample example);

    int updateByExample(@Param("record") MarketingMockTest record, @Param("example") MarketingMockTestExample example);

    int updateByPrimaryKeySelective(MarketingMockTest record);

    int updateByPrimaryKey(MarketingMockTest record);
}