package com.br.marketing.service;

import com.br.marketing.entity.MarketingCustomerAssignedGroup;
import com.br.marketing.entity.MarketingCustomerAssignedGroupExample;

import java.util.List;
import java.util.Set;

public interface IMarketingCustomerAssignedGroupService {

    void assignGroup(String cid, String assignedGroup, String apiCode);

    String getAssignedGroupByApiCode(String apiCode);

    Set<String> getAssignedGroups();

    List<MarketingCustomerAssignedGroup> selectByExample(MarketingCustomerAssignedGroupExample example);

}
