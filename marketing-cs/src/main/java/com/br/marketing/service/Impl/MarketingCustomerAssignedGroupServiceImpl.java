package com.br.marketing.service.Impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.CustomerEnum;
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
import java.util.*;

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
    public void assignGroup(String cid, String group, String apiCode) {
        CustomerEnum customerType = getCustomerEnum(cid, apiCode);
        try {
            MarketingCustomerAssignedGroup assignedGroup = marketingCustomerAssignedGroupMapper.getAssignedGroupByCid(cid);
            if (Objects.isNull(assignedGroup)) {
                createNewGroup(cid, customerType);
            } else {
                if (StringUtils.equals(assignedGroup.getAssignedGroup(), group)) {
                    return;
                }
                List<String> assignedGroups = marketingCommonConfig.getAssignedGroups();
                int nextIndex;
                if (StringUtils.isEmpty(group)) {
                    return;
                } else {
                    nextIndex = assignedGroups.indexOf(group);
                }
                MarketingCustomerAssignedGroup marketingCustomerAssignedGroup = new MarketingCustomerAssignedGroup();
                marketingCustomerAssignedGroup.setCid(cid);
                marketingCustomerAssignedGroup.setCurrentIndex(nextIndex);
                marketingCustomerAssignedGroup.setAssignedGroup(group);
                marketingCustomerAssignedGroup.setCustomerType(customerType.getCode());
                marketingCustomerAssignedGroup.setUpdateTime(new Date());

                MarketingCustomerAssignedGroupExample example = new MarketingCustomerAssignedGroupExample();
                example.createCriteria().andCidEqualTo(cid);
                marketingCustomerAssignedGroupMapper.updateByExample(marketingCustomerAssignedGroup, example);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.POLLING_GROUP_EXCEPTION.getCode(), "项目轮询开发组异常"));
        }
    }

    private static CustomerEnum getCustomerEnum(String cid, String apiCode) {
        if (StringUtils.startsWith(apiCode, "4")) {
            return CustomerEnum.INNER_TEST;
        } else if (StringUtils.startsWith(apiCode, "3") && !StringUtils.startsWith(cid, "-")) {
            return CustomerEnum.PROD;
        } else {
            return CustomerEnum.TEST;
        }
    }

    private void createNewGroup(String cid, CustomerEnum customerType) {
        MarketingCustomerAssignedGroup preAssignedGroup = marketingCustomerAssignedGroupMapper.getLastAssignedGroup(cid, customerType.getCode());
        int nextIndex;
        List<String> assignedGroups = marketingCommonConfig.getAssignedGroups();
        if(Objects.nonNull(preAssignedGroup)) {
            nextIndex = (preAssignedGroup.getCurrentIndex() + 1) % assignedGroups.size();
        } else {
            nextIndex = 0;
        }
        String group = assignedGroups.get(nextIndex);
        MarketingCustomerAssignedGroup marketingCustomerAssignedGroup = new MarketingCustomerAssignedGroup();
        marketingCustomerAssignedGroup.setCid(cid);
        marketingCustomerAssignedGroup.setAssignedGroup(group);
        marketingCustomerAssignedGroup.setCurrentIndex(nextIndex);
        marketingCustomerAssignedGroup.setCustomerType(customerType.getCode());
        marketingCustomerAssignedGroup.setCreateTime(new Date());
        marketingCustomerAssignedGroup.setUpdateTime(new Date());
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
        return new HashSet<>(marketingCommonConfig.getAssignedGroups());
    }

    @Override
    public List<MarketingCustomerAssignedGroup> selectByExample(MarketingCustomerAssignedGroupExample example) {
        return marketingCustomerAssignedGroupMapper.selectByExample(example);
    }
}
