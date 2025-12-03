package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCustomerAssignedGroup;
import com.br.marketing.entity.MarketingCustomerAssignedGroupExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingCustomerAssignedGroupMapper extends MarketingCustomerAssignedGroupMapperBase{

    MarketingCustomerAssignedGroup getAssignedGroupByCid(@Param("cid") String cid);

    MarketingCustomerAssignedGroup getLastAssignedGroup(@Param("cid") String cid, @Param("customerType") Integer customerType);

}