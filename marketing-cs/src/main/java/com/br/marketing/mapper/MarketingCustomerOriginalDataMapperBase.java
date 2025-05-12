package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCustomerOriginalData;
import com.br.marketing.entity.MarketingCustomerOriginalDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingCustomerOriginalDataMapperBase {
    long countByExample(MarketingCustomerOriginalDataExample example);

    int deleteByExample(MarketingCustomerOriginalDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCustomerOriginalData record);

    int insertSelective(MarketingCustomerOriginalData record);

    List<MarketingCustomerOriginalData> selectByExample(MarketingCustomerOriginalDataExample example);

    MarketingCustomerOriginalData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCustomerOriginalData record, @Param("example") MarketingCustomerOriginalDataExample example);

    int updateByExample(@Param("record") MarketingCustomerOriginalData record, @Param("example") MarketingCustomerOriginalDataExample example);

    int updateByPrimaryKeySelective(MarketingCustomerOriginalData record);

    int updateByPrimaryKey(MarketingCustomerOriginalData record);
}