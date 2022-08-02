package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingCustomerMapperBase {
    int countByExample(MarketingCustomerExample example);

    int deleteByExample(MarketingCustomerExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCustomer record);

    int insertSelective(MarketingCustomer record);

    @AddDataAuth
    List<MarketingCustomer> selectByExample(MarketingCustomerExample example);

    MarketingCustomer selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCustomer record, @Param("example") MarketingCustomerExample example);

    int updateByExample(@Param("record") MarketingCustomer record, @Param("example") MarketingCustomerExample example);

    int updateByPrimaryKeySelective(MarketingCustomer record);

    int updateByPrimaryKey(MarketingCustomer record);
}