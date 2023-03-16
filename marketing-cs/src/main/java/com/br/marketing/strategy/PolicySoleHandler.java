package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
/**
 * @Description : 推送决策去重调用方法
 * @Author : lizhen
 * @Date : Create in 2023/03/13 16:11
 */
public class PolicySoleHandler extends AbstractExternalInterfaceHandler<PushMarketingUserDetailByRuleDTO> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public JSONObject call(List<PushMarketingUserDetailByRuleDTO> policyByRuleList, ProcessHandlerContext context) {

        Map<String, List<PushMarketingUserDetailByRuleDTO>> batchMap = policyByRuleList.stream().collect(Collectors.groupingBy(PushMarketingUserDetailByRuleDTO::getBatchNumber));
        for (String batch : batchMap.keySet()) {
            //数据日志数组
            ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
            List<PushMarketingUserDetailByRuleDTO> ruleLists = batchMap.get(batch);
            ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
            List<Long> sourceIds = new ArrayList<>();
            List<String> list = Arrays.asList(batch.split("_"));
            String status = "";
            if (!CollectionUtils.isEmpty(list)) {
                String statusSub = list.get(0);
                status = statusSub.substring(statusSub.length() - 1);
            }
            for(PushMarketingUserDetailByRuleDTO t : ruleLists) {
                PushMarketingUserDetailDTO entity = new PushMarketingUserDetailDTO();
                BeanUtils.copyProperties(t, entity);
                pushs.add(entity);
                sourceIds.add(t.getInitId());
                // 把封装的日志插入到数组中
                logList.add(methodRetryHandlerService.dataJoinLogFix(entity, DistributeTypeEnum.POLICYDATA
                        , context.getApiCode(), t.getPhone(), t.getPhone()
                        , Long.valueOf(t.getInitId()), DistributeSourceTypeEnum.TRANSFER, status));

            }
            PolicyRetryByRuleSoleDTO retryByRuleDTO = new PolicyRetryByRuleSoleDTO();
            retryByRuleDTO.setApiCode(context.getApiCode());
            retryByRuleDTO.setBatchNumber(batch);
            retryByRuleDTO.setStrategyCode(ruleLists.get(0).getStrategyCode());
            retryByRuleDTO.setIds(sourceIds);
            retryByRuleDTO.setInfoId(context.getMqFact().getSourceId());
            retryByRuleDTO.setData(pushs);
            retryByRuleDTO.setDetailLogList(logList);
            //传参去重
            retryByRuleDTO.setIsSole(true);
            //2-根据apicode cell,status 维度去重
            retryByRuleDTO.setSoleField(3);
            //去重数据范围1-是当天，其他值则now()-(day-1)
            if (marketingCommonConfig.getXieChengPushPolicyValidityDay() != null) {
                retryByRuleDTO.setSoleDay(marketingCommonConfig.getXieChengPushPolicyValidityDay());
            } else {
                retryByRuleDTO.setSoleDay(1);
            }
            methodRetryHandlerService.callPolicySoleData(retryByRuleDTO, 0);
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.INIT_TO_POLICY_SOLE;
    }

}
