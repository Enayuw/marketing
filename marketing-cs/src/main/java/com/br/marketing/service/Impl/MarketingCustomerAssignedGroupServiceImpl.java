package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerAssignedGroup;
import com.br.marketing.entity.MarketingCustomerAssignedGroupExample;
import com.br.marketing.mapper.MarketingCustomerAssignedGroupMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.service.IMarketingCustomerAssignedGroupService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class MarketingCustomerAssignedGroupServiceImpl implements IMarketingCustomerAssignedGroupService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private MarketingCustomerAssignedGroupMapper marketingCustomerAssignedGroupMapper;

    @Override
    public void assignGroup(String cid, String group) {
        try {
            MarketingCustomerAssignedGroup assignedGroup = marketingCustomerAssignedGroupMapper.getAssignedGroupByCid(cid);
            if (Objects.isNull(assignedGroup)) {
                createNewGroup(cid);
            } else {
                if (StringUtils.equals(assignedGroup.getAssignedGroup(), group)) {
                    return;
                }
                if (StringUtils.isEmpty(group)) {
                    String preAssignedGroup = marketingCustomerAssignedGroupMapper.getLastAssignedGroup();
                    group = marketingCommonConfig.getAssignedGroupMap().get(preAssignedGroup);
                }
                MarketingCustomerAssignedGroup marketingCustomerAssignedGroup = new MarketingCustomerAssignedGroup();
                marketingCustomerAssignedGroup.setCid(cid);
                marketingCustomerAssignedGroup.setAssignedGroup(group);
                marketingCustomerAssignedGroupMapper.updateByCid(cid, group);
            }
        } catch (Exception e) {
            log.warn("项目轮询开发组异常", e);
        }
    }

    private void createNewGroup(String cid) {
        String preAssignedGroup = marketingCustomerAssignedGroupMapper.getLastAssignedGroup();
        String group = marketingCommonConfig.getAssignedGroupMap().get(preAssignedGroup);
        MarketingCustomerAssignedGroup marketingCustomerAssignedGroup = new MarketingCustomerAssignedGroup();
        marketingCustomerAssignedGroup.setCid(cid);
        marketingCustomerAssignedGroup.setAssignedGroup(group);
        marketingCustomerAssignedGroupMapper.insertSelective(marketingCustomerAssignedGroup);
    }

    @Override
    public String getAssignedGroupByApiCode(String apiCode) {
        List<MarketingCustomer> customers = marketingCustomerMapper.getCidByApiCode(apiCode);
        if (CollectionUtils.isEmpty(customers)) {
            return null;
        }
        String cid = customers.get(0).getCid();
        return marketingCustomerAssignedGroupMapper.getAssignedGroupByCid(cid).getAssignedGroup();
    }

    @Override
    public Set<String> getAssignedGroups() {
        return marketingCommonConfig.getAssignedGroupMap().keySet();
    }

    @Override
    public List<MarketingCustomerAssignedGroup> selectByExample(MarketingCustomerAssignedGroupExample example) {
        return marketingCustomerAssignedGroupMapper.selectByExample(example);
    }
}
