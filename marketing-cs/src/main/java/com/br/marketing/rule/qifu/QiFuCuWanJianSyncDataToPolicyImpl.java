package com.br.marketing.rule.qifu;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingCustomizeDataValidConfig;
import com.br.marketing.entity.MarketingCustomizeDataValidConfigExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingCustomizeDataValidConfigMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 360促完件T0场景-3710147 上传数据推决策
 *
 * @author senyang.zheng
 * @date 2024/06/27
 */
@Service
@Slf4j
public class QiFuCuWanJianSyncDataToPolicyImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private MarketingCustomizeDataValidConfigMapper customizeDataValidConfigMapper;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingSyncUser syncUser = (MarketingSyncUser)transmitFact;
        PushMarketingUserDetailByRuleDTO dto = new PushMarketingUserDetailByRuleDTO();
        dto.setInitId(syncUser.getId());
        dto.setBatchNumber(DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT) + "_" + syncUser.getApiCode());
        dto.setStrategyCode(marketingCommonConfig.getQiFuToPolicyStrategyCodeConfig().getString(syncUser.getApiCode()));
        dto.setCaseNumber(syncUser.getCustNum());
        dto.setPhone(syncUser.getCellMd5());
        JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
        JSONObject variables = new JSONObject();
        for (String key : jsonObject.keySet()) {
            variables.put(key, jsonObject.get(key));
        }
        dto.setVariables(variables);
        return dto;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingSyncUser syncUser = (MarketingSyncUser)transmitFact;
        MarketingCustomizeDataValidConfigExample example = new MarketingCustomizeDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(syncUser.getApiCode()).andTaskIdEqualTo(syncUser.getCusBatch()).andIsDelEqualTo(1)
            .andValidStartDateLessThanOrEqualTo(DateUtil.today()).andValidEndDateGreaterThanOrEqualTo(DateUtil.today());
        List<MarketingCustomizeDataValidConfig> configList = customizeDataValidConfigMapper.selectByExample(example);
        return CollectionUtil.isNotEmpty(configList);
    }

    @Override
    public String label() {
        return "QiFu_CuWanJian_Sync_Data_To_Policy";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.INIT_TO_POLICY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
