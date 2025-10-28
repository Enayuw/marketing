package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCustomerAssignedGroup;
import com.br.marketing.entity.MarketingCustomerAssignedGroupExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingCustomerAssignedGroupMapperBase {
    int countByExample(MarketingCustomerAssignedGroupExample example);

    int deleteByExample(MarketingCustomerAssignedGroupExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCustomerAssignedGroup record);

    int insertSelective(MarketingCustomerAssignedGroup record);

    List<MarketingCustomerAssignedGroup> selectByExample(MarketingCustomerAssignedGroupExample example);

    MarketingCustomerAssignedGroup selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCustomerAssignedGroup record,
                                 @Param("example") MarketingCustomerAssignedGroupExample example);

    int updateByExample(@Param("record") MarketingCustomerAssignedGroup record,
                        @Param("example") MarketingCustomerAssignedGroupExample example);

    int updateByPrimaryKeySelective(MarketingCustomerAssignedGroup record);

    int updateByPrimaryKey(MarketingCustomerAssignedGroup record);
}