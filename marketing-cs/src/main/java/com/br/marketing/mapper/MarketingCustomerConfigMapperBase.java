package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCustomerConfig;
import com.br.marketing.entity.MarketingCustomerConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingCustomerConfigMapperBase {
    int countByExample(MarketingCustomerConfigExample example);

    int deleteByExample(MarketingCustomerConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCustomerConfig record);

    int insertSelective(MarketingCustomerConfig record);

    List<MarketingCustomerConfig> selectByExample(MarketingCustomerConfigExample example);

    MarketingCustomerConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCustomerConfig record, @Param("example") MarketingCustomerConfigExample example);

    int updateByExample(@Param("record") MarketingCustomerConfig record, @Param("example") MarketingCustomerConfigExample example);

    int updateByPrimaryKeySelective(MarketingCustomerConfig record);

    int updateByPrimaryKey(MarketingCustomerConfig record);
}