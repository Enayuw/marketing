package com.br.marketing.rule.qifu;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import cn.hutool.core.date.DateTime;
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
        if (syncUser.getStatus() != 1) {
            return false;
        }
        List<MarketingCustomizeDataValidConfig> configList = getValidConfigList(syncUser);
        if (CollectionUtil.isEmpty(configList)) {
            log.error("奇富360促完件上传数据推决策，未查询到该条上传数据有效期配置：apiCode:{},appletDate:{},userType:{},taskId:{}", syncUser.getApiCode(),
                syncUser.getAppletDate(), syncUser.getUserType(), syncUser.getCusBatch());
            return false;
        }
        DateTime currentDate = DateUtil.date();
        for (MarketingCustomizeDataValidConfig config : configList) {
            DateTime startDate = DateUtil.parse(config.getValidStartDate());
            DateTime endDate = DateUtil.parse(config.getValidEndDate());
            if (currentDate.isAfterOrEquals(startDate) && currentDate.isBeforeOrEquals(endDate)) {
                return true;
            }
        }
        return false;
    }

    private List<MarketingCustomizeDataValidConfig> getValidConfigList(MarketingSyncUser syncUser) {
        MarketingCustomizeDataValidConfigExample example = new MarketingCustomizeDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(syncUser.getApiCode()).andUserTypeEqualTo(syncUser.getUserType())
            .andAppletDateEqualTo(syncUser.getAppletDate()).andTaskIdEqualTo(syncUser.getCusBatch()).andIsDelEqualTo(1);
        List<MarketingCustomizeDataValidConfig> configList = customizeDataValidConfigMapper.selectByExample(example);
        if (CollectionUtil.isEmpty(configList)) {
            log.warn("奇富360促完件上传数据推决策该上传数据未查询到有效配置，id:{}，等待 {} 秒后重试", syncUser.getId(),
                marketingCommonConfig.getQiFuSyncToPolicyValidityCheckDelayTime());
            try {
                TimeUnit.SECONDS.sleep(marketingCommonConfig.getQiFuSyncToPolicyValidityCheckDelayTime());
            } catch (InterruptedException e) {
                log.error("奇富360促完件上传数据推决策，未查询到有效期配置等待异常", e);
                return Collections.emptyList();
            }
            configList = customizeDataValidConfigMapper.selectByExample(example);
        }
        return configList;
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
