package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCustomerInitialData;
import com.br.marketing.entity.MarketingCustomerInitialDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingCustomerInitialDataMapperBase {
    int countByExample(MarketingCustomerInitialDataExample example);

    int deleteByExample(MarketingCustomerInitialDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCustomerInitialData record);

    int insertSelective(MarketingCustomerInitialData record);

    List<MarketingCustomerInitialData> selectByExample(MarketingCustomerInitialDataExample example);

    MarketingCustomerInitialData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCustomerInitialData record, @Param("example") MarketingCustomerInitialDataExample example);

    int updateByExample(@Param("record") MarketingCustomerInitialData record, @Param("example") MarketingCustomerInitialDataExample example);

    int updateByPrimaryKeySelective(MarketingCustomerInitialData record);

    int updateByPrimaryKey(MarketingCustomerInitialData record);
}